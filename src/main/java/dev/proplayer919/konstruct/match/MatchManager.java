package dev.proplayer919.konstruct.match;

import dev.proplayer919.konstruct.instance.GameInstanceData;
import dev.proplayer919.konstruct.instance.HubInstanceData;
import dev.proplayer919.konstruct.instance.HubInstanceRegistry;
import dev.proplayer919.konstruct.instance.gameplayer.GamePlayerData;
import dev.proplayer919.konstruct.messages.MatchMessages;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class MatchManager {
    public static void spawnPlayerIntoMatch(GameInstanceData gameInstanceData, Player player) {
        HubInstanceData hubInstanceData = HubInstanceRegistry.getInstanceWithPlayer(player.getUuid());
        if (hubInstanceData != null) {
            hubInstanceData.getPlayers().remove(player);
        } else {
            throw new IllegalStateException("Player is not in any hub instance");
        }

        GamePlayerData gamePlayerData = new GamePlayerData(player.getUuid());
        gameInstanceData.addPlayer(gamePlayerData);

        player.setInstance(gameInstanceData.getInstance());
        player.setEnableRespawnScreen(false);
        player.setRespawnPoint(gameInstanceData.getMatchType().getSpectatorSpawn());
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(gameInstanceData.getMatchType().getWaitingSpawn());

        // Send a message to all players in the match that a new player has joined
        for (GamePlayerData pData : gameInstanceData.getPlayers()) {
            Player p = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(pData.getUuid());
            if (p != null) {
                p.sendMessage(MatchMessages.createPlayerJoinedMessage(p.getUsername(), gameInstanceData.getPlayers().size(), gameInstanceData.getMatchType().getMaxPlayers()));
            }
        }
    }
}
