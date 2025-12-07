package dev.proplayer919.konstruct.instance;

import dev.proplayer919.konstruct.instance.gameplayer.GamePlayerData;
import dev.proplayer919.konstruct.instance.gameplayer.GamePlayerStatus;
import dev.proplayer919.konstruct.match.MatchStatus;
import dev.proplayer919.konstruct.match.types.MatchType;
import dev.proplayer919.konstruct.messages.MatchMessages;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import java.util.*;

@Getter
public class GameInstanceData extends InstanceData {
    private final UUID hostUUID;
    private final MatchType matchType;
    private final Date startTime = new Date(System.currentTimeMillis() + 300000); // Default to 5 minutes from now

    @Setter
    private MatchStatus matchStatus = MatchStatus.WAITING;

    private final Collection<GamePlayerData> players = new HashSet<>();

    public GameInstanceData(String id, UUID hostUUID, MatchType matchType) {
        super(InstanceType.GAME, matchType.getInstance(), id);
        this.hostUUID = hostUUID;
        this.matchType = matchType;

        // Setup events
        matchType.getInstance().eventNode().addListener(PlayerDisconnectEvent.class, event -> {
            // Handle player disconnect
            GamePlayerData playerData = this.players.stream()
                    .filter(p -> p.getUuid().equals(event.getPlayer().getUuid()))
                    .findFirst()
                    .orElse(null);
            if (playerData != null) {
                switch (matchStatus) {
                    case WAITING -> {
                        players.remove(playerData);
                        sendMessageToAllPlayers(MatchMessages.createPlayerLeftMessage(event.getPlayer().getUsername(), players.size(), matchType.getMaxPlayers()));
                    }
                    case IN_PROGRESS -> {
                        playerData.setStatus(GamePlayerStatus.DEAD);
                        sendMessageToAllPlayers(MatchMessages.createPlayerDisconnectMessage(event.getPlayer().getUsername()));
                    }
                }
            }
        });

        matchType.getInstance().eventNode().addListener(PlayerDeathEvent.class, event -> {
            // Handle player death
            GamePlayerData playerData = this.players.stream()
                    .filter(p -> p.getUuid().equals(event.getPlayer().getUuid()))
                    .findFirst()
                    .orElse(null);
            if (playerData != null && playerData.isAlive()) {
                playerData.setStatus(GamePlayerStatus.DEAD);
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                sendMessageToAllPlayers(MatchMessages.createPlayerEliminatedMessage(event.getEntity().getUsername(), getAlivePlayers().size()));
            }
        });
    }

    public void sendMessageToAllPlayers(Component message) {
        for (GamePlayerData player : players) {
            Objects.requireNonNull(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(player.getUuid())).sendMessage(message);
        }
    }

    public boolean isFull() {
        return players.size() >= matchType.getMaxPlayers();
    }

    public boolean hasEnoughPlayers() {
        return players.size() >= matchType.getMinPlayers();
    }

    public int addPlayer(GamePlayerData player) {
        players.add(player);
        return players.size() - 1;
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
