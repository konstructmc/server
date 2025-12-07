package dev.proplayer919.konstruct.instance;

import dev.proplayer919.konstruct.instance.gameplayer.GamePlayerData;
import net.minestom.server.instance.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameInstanceRegistry {
    private static final Map<String, GameInstanceData> instances = new HashMap<>();

    public static String getNextInstanceId() {
        // Instance IDs are in the format "match-<three-digit number>"
        // e.g. match-000, match-001, match-002, ...
        return "match-" + String.format("%03d", instances.size());
    }

    public static void registerInstance(GameInstanceData instance) {
        instances.put(instance.getId(), instance);
    }

    public static GameInstanceData getInstanceById(String id) {
        return instances.get(id);
    }

    public static GameInstanceData getInstanceWithPlayer(UUID playerUUID) {
        for (GameInstanceData gameInstanceData : instances.values()) {
            for (GamePlayerData gamePlayerData : gameInstanceData.getPlayers()) {
                if (gamePlayerData.getUuid().equals(playerUUID)) {
                    return gameInstanceData;
                }
            }
        }
        return null;
    }

    public static GameInstanceData getInstanceByInstance (Instance instance) {
        for (GameInstanceData gameInstanceData : instances.values()) {
            if (gameInstanceData.getInstance().equals(instance)) {
                return gameInstanceData;
            }
        }
        return null;
    }

    public static void removeInstanceById(String id) {
        instances.remove(id);
    }

    public static Map<String, GameInstanceData> getAllInstances() {
        return instances;
    }
}
