package dev.proplayer919.konstruct.matches;

import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.loot.ChestLootRegistry;
import dev.proplayer919.konstruct.util.PlayerInventoryBlockRegistry;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class MatchData {
    private final UUID matchUUID = UUID.randomUUID();

    private final UUID hostUUID;
    private final String hostUsername;

    private final Collection<MatchPlayer> players = new ArrayList<>();
    private final Instance lobbyInstance;
    private final Instance matchInstance;

    private final Map<MatchPlayer, MatchPlayer> playerAttackers = new HashMap<>();

    @Setter
    private MatchStatus status = MatchStatus.WAITING;

    private final ChestLootRegistry chestLootRegistry = new ChestLootRegistry();
    private final PlayerInventoryBlockRegistry inventoryBlockRegistry =  new PlayerInventoryBlockRegistry();
    private final Date startTime = new Date(System.currentTimeMillis() + Duration.ofSeconds(20).toMillis()); // Default to 5 minutes from now

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Pos lobbySpawn = new Pos(0.5, 40, 0.5);
    private final Pos spectatorSpawn = new Pos(0.5, 60, 0.5);

    private final int minPlayers = 2;
    private final int maxPlayers = 16;

    private final Pos buildingBounds1 = new Pos(-150, 35, -150);
    private final Pos buildingBounds2 = new Pos(150, 60, 150);

    public MatchData(CustomPlayer host, Instance lobbyInstance, Instance matchInstance) {
        this.hostUUID = host.getUuid();
        this.hostUsername = host.getUsername();

        this.lobbyInstance = lobbyInstance;
        this.matchInstance = matchInstance;
    }

    public boolean isPlayerAlive(MatchPlayer player) {
        return players.contains(player) && player.isAlive();
    }

    public Collection<MatchPlayer> getAlivePlayers() {
        Collection<MatchPlayer> alivePlayers = new ArrayList<>();
        for (MatchPlayer player : players) {
            if (player.isAlive()) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getAlivePlayerCount() {
        return getAlivePlayers().size();
    }

    public Collection<MatchPlayer> getSpectators() {
        Collection<MatchPlayer> spectators = new ArrayList<>();
        for (MatchPlayer player : players) {
            if (!player.isAlive()) {
                spectators.add(player);
            }
        }
        return spectators;
    }

    public int getSpectatorCount() {
        return getSpectators().size();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean hasEnoughPlayers() {
        return players.size() >= minPlayers;
    }

    public void addPlayer(MatchPlayer player) {
        this.players.add(player);
    }

    public void removePlayer(MatchPlayer player) {
        this.players.remove(player);
    }


}
