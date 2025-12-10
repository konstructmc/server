package dev.proplayer919.konstruct.instance;

import dev.proplayer919.konstruct.instance.gameplayer.GamePlayerData;
import dev.proplayer919.konstruct.instance.gameplayer.GamePlayerStatus;
import dev.proplayer919.konstruct.match.MatchManager;
import dev.proplayer919.konstruct.match.MatchStatus;
import dev.proplayer919.konstruct.match.types.MatchType;
import dev.proplayer919.konstruct.messages.MatchMessages;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import io.github.togar2.pvp.events.EntityPreDeathEvent;
import io.github.togar2.pvp.events.FinalAttackEvent;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class GameInstanceData extends InstanceData {
    private final UUID hostUUID;
    private final String hostUsername;
    private final MatchType matchType;
    private final Date startTime = new Date(System.currentTimeMillis() + 300000); // Default to 5 minutes from now

    @Setter
    private MatchStatus matchStatus = MatchStatus.WAITING;

    private final Collection<GamePlayerData> players = new HashSet<>();

    public GameInstanceData(String id, UUID hostUUID, MatchType matchType) {
        super(InstanceType.GAME, matchType.getInstance(), id);
        this.hostUUID = hostUUID;
        this.hostUsername = Objects.requireNonNull(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(hostUUID)).getUsername();
        this.matchType = matchType;

        // Setup schedules
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            // Don't advertise in the minute where the match starts
            long millisUntilStart = getStartTime().getTime() - System.currentTimeMillis();
            if (getMatchStatus() == MatchStatus.WAITING && isNotFull()
                    && millisUntilStart >= 60000) {
                Collection<Audience> playersInHubs = HubInstanceRegistry.getAllPlayersInHubs()
                        .stream()
                        .map(p -> (Audience) p)
                        .collect(Collectors.toList());
                MessagingHelper.sendMessage(playersInHubs, MatchMessages.createMatchAdvertiseMessage(id, hostUsername, matchType.getName(), startTime));

                // Send a message to people in the match as well
                sendMessageToAllPlayers(MatchMessages.createCountdownMessage(startTime, players.size(), matchType.getMinPlayers()));
            }
        }).repeat(Duration.ofMinutes(1)).schedule();

        // Schedule a task that runs 5 seconds before the match starts
        Duration preMatchCountdownDelay = Duration.ofMillis(startTime.getTime() - System.currentTimeMillis() - 5000);
        MinecraftServer.getSchedulerManager().buildTask(this::startPreMatchCountdown).delay(preMatchCountdownDelay).schedule();

        // Setup events
        matchType.getInstance().eventNode().addListener(PlayerDisconnectEvent.class, event -> {
            // Handle player disconnect
            MatchManager.playerLeaveMatch(this, event.getPlayer());
        });

        matchType.getInstance().eventNode().addListener(EntityPreDeathEvent.class, event -> {
            // Handle player death
            event.setCancelled(true);
        });

        matchType.getInstance().eventNode().addListener(FinalAttackEvent.class, event -> {
            // If the match is not in progress, cancel the attack
            if (this.matchStatus != MatchStatus.IN_PROGRESS) {
                event.setCancelled(true);
            }

            // Find if the attack is fatal
            Entity target = event.getTarget();
            if (target instanceof Player player) {
                GamePlayerData playerData = this.players.stream()
                        .filter(p -> p.getUuid().equals(player.getUuid()))
                        .findFirst()
                        .orElse(null);
                if (playerData != null && playerData.isAlive()) {
                    double finalDamage = event.getBaseDamage() + event.getEnchantsExtraDamage();
                    if (finalDamage >= player.getHealth()) {
                        Entity killer = event.getEntity();
                        if (killer instanceof Player killerPlayer) {
                            killPlayer(playerData);

                            // This attack would be fatal, so trigger the elimination message (but the actual death will be handled in EntityPreDeathEvent)
                            if (getAlivePlayers().size() == 1) {
                                // Find the killer's player data
                                GamePlayerData killerData = this.players.stream()
                                        .filter(p -> p.getUuid().equals(killerPlayer.getUuid()))
                                        .findFirst()
                                        .orElse(null);
                                if (killerData != null) {
                                    winMatch(killerData);
                                }
                            } else {
                                sendMessageToAllPlayers(MatchMessages.createPlayerEliminatedMessage(player.getUsername(), killerPlayer.getUsername(), getAlivePlayers().size() - 1));
                            }

                            Component killerMessage = MatchMessages.createKillerMessage(player.getUsername());
                            killerPlayer.sendActionBar(killerMessage);

                            Sound killerSound = Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.AMBIENT, 1.0f, 1.0f);
                            killerPlayer.playSound(killerSound);
                        }
                    }
                }
            }
        });

        matchType.getInstance().eventNode().addListener(PlayerMoveEvent.class, event -> {
            // If the match is in countdown, prevent movement
            if (this.matchStatus == MatchStatus.COUNTDOWN) {
                // TODO: fix this (it still allows movement)
                event.setCancelled(true);
            }

            // If the player is below Y=0 and are alive while the match is in progress, kill them
            if (this.matchStatus == MatchStatus.IN_PROGRESS) {
                Player player = event.getPlayer();
                GamePlayerData playerData = this.players.stream()
                        .filter(p -> p.getUuid().equals(player.getUuid()))
                        .findFirst()
                        .orElse(null);
                if (playerData != null && playerData.isAlive()) {
                    if (player.getPosition().y() < 0) {
                        killPlayer(playerData);

                        if (getAlivePlayers().size() == 1) {
                            GamePlayerData winnerData = getAlivePlayers().iterator().next();
                            winMatch(winnerData);
                        } else {
                            sendMessageToAllPlayers(MatchMessages.createPlayerVoidMessage(player.getUsername(), getAlivePlayers().size() - 1));
                        }
                    }
                }
            }
        });
    }

    public void startPreMatchCountdown() {
        // Start a new task for the 5-second countdown
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            int countdown = 5;
            while (countdown > 0) {
                Component message = MatchMessages.createCountdownMessage(startTime, players.size(), matchType.getMinPlayers());
                sendMessageToAllPlayers(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                countdown--;
            }

            startMatch();
        }).schedule();
    }

    public void startMatchCountdown() {
        setMatchStatus(MatchStatus.COUNTDOWN);

        // Start a new task for the 10-second countdown
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            int countdown = 10;
            while (countdown > 0) {
                Component actionbarMessage = Component.text("Get ready to go in ", NamedTextColor.YELLOW)
                        .append(Component.text(countdown + " seconds!", NamedTextColor.GOLD));
                sendActionbarToAllPlayers(actionbarMessage);
                sendSoundToAllPlayers(Sound.sound(Key.key("minecraft:block.note_block.bell"), Sound.Source.AMBIENT, 1.0f, 1.0f));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                countdown--;
            }

            // Final message
            Component goMessage = Component.text("GO!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
            sendActionbarToAllPlayers(goMessage);
            sendSoundToAllPlayers(Sound.sound(Key.key("minecraft:entity.firework_rocket.launch"), Sound.Source.AMBIENT, 1.0f, 1.0f));

            setMatchStatus(MatchStatus.IN_PROGRESS);
        }).schedule();
    }

    public void startMatch() {
        if (getMatchStatus() == MatchStatus.WAITING) {
            if (hasEnoughPlayers()) {
                // Teleport all players
                int playerIndex = 0;
                for (GamePlayerData gamePlayerData : players) {
                    Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayerData.getUuid());
                    if (player != null) {
                        Pos spawnPos = matchType.getSpawnPointForPlayer(playerIndex, players.size());
                        player.teleport(spawnPos);
                        player.setGameMode(GameMode.SURVIVAL);
                        playerIndex++;
                    }
                }

                // Start the match countdown
                startMatchCountdown();
            } else {
                // Not enough players, cancel the match
                tooLittlePlayers();
            }
        }
    }

    public void tooLittlePlayers() {
        sendMessageToAllPlayers(MatchMessages.createMatchTooLittlePlayersMessage(matchType.getMinPlayers()));

        packupMatch();
    }

    public void killPlayer(GamePlayerData playerData) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerData.getUuid());
        if (player != null && playerData.isAlive()) {
            Pos deathPos = player.getPosition();

            Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
            lightning.setInstance(getMatchType().getInstance(), deathPos);

            player.setVelocity(Vec.ZERO);

            playerData.setStatus(GamePlayerStatus.DEAD);
            player.setGameMode(GameMode.SPECTATOR);

            // For all players, remove them from viewing the player
            for (GamePlayerData otherPlayerData : getPlayers()) {
                if (!otherPlayerData.getUuid().equals(playerData.getUuid())) {
                    Player otherPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(otherPlayerData.getUuid());
                    if (otherPlayer != null) {
                        player.removeViewer(otherPlayer);
                    }
                }
            }

            player.teleport(matchType.getSpectatorSpawn());
        }
    }

    public void winMatch(GamePlayerData playerData) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerData.getUuid());
        if (player != null && playerData.isAlive()) {
            setMatchStatus(MatchStatus.ENDED);

            Component winMessage = MatchMessages.createWinnerMessage(player.getUsername());
            sendMessageToAllPlayers(winMessage);

            // Play victory sound to the winner
            Sound victorySound = Sound.sound(Key.key("minecraft:item.totem.use"), Sound.Source.AMBIENT, 1.0f, 1.0f);
            player.playSound(victorySound);

            // After a short delay, pack up the match
            MinecraftServer.getSchedulerManager().buildTask(this::packupMatch).delay(Duration.ofSeconds(5)).schedule();
        }
    }

    public void packupMatch() {
        for (GamePlayerData gamePlayerData : players) {
            Player p = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayerData.getUuid());
            if (p != null) {
                // Find the hub with the least players and send the player there
                HubInstanceData hubInstanceData = HubInstanceRegistry.getInstanceWithLowestPlayers();
                if (hubInstanceData != null) {
                    hubInstanceData.getPlayers().add(p);
                    p.setInstance(hubInstanceData.getInstance());
                    p.teleport(new Pos(0.5, 40, 0.5)); // Teleport to hub spawn point
                    p.setGameMode(GameMode.SURVIVAL);
                    p.setHealth(20);
                    MessagingHelper.sendMessage(p, MessageType.SERVER, "You have been returned to the hub.");
                } else {
                    p.kick("No hub instance available. Please try reconnecting later.");
                }
            }
        }

        // De-register this instance
        GameInstanceRegistry.removeInstanceById(getId());
    }

    public void sendMessageToAllPlayers(Component message) {
        for (GamePlayerData player : players) {
            Objects.requireNonNull(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(player.getUuid())).sendMessage(message);
        }
    }

    public void sendActionbarToAllPlayers(Component message) {
        for (GamePlayerData player : players) {
            Objects.requireNonNull(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(player.getUuid()))
                    .sendActionBar(message);
        }
    }

    public void sendSoundToAllPlayers(Sound sound) {
        for (GamePlayerData player : players) {
            Objects.requireNonNull(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(player.getUuid()))
                    .playSound(sound);
        }
    }

    public boolean isNotFull() {
        return players.size() < matchType.getMaxPlayers();
    }

    public boolean isFull() {
        return players.size() == matchType.getMaxPlayers();
    }

    public boolean hasEnoughPlayers() {
        return players.size() >= matchType.getMinPlayers();
    }

    public void addPlayer(GamePlayerData player) {
        players.add(player);

        // Check if we have enough players to start the match early
        if (isFull() && getMatchStatus() == MatchStatus.WAITING) {
            // Start the pre-match countdown immediately
            startPreMatchCountdown();
        }
    }

    public Collection<GamePlayerData> getAlivePlayers() {
        Collection<GamePlayerData> alivePlayers = new HashSet<>();
        for (GamePlayerData player : players) {
            if (player.isAlive()) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers;
    }

    public Collection<GamePlayerData> getDeadPlayers() {
        Collection<GamePlayerData> deadPlayers = new HashSet<>();
        for (GamePlayerData player : players) {
            if (player.isDead()) {
                deadPlayers.add(player);
            }
        }
        return deadPlayers;
    }

    public Collection<GamePlayerData> getSpectators() {
        Collection<GamePlayerData> spectators = new HashSet<>();
        for (GamePlayerData player : players) {
            if (player.isSpectating()) {
                spectators.add(player);
            }
        }
        return spectators;
    }

}
