package dev.proplayer919.konstruct.hubs;

import dev.proplayer919.konstruct.CustomPlayer;
import net.minestom.server.instance.Instance;

import java.util.*;

public class HubRegistry {
    private static final Map<String, HubData> instances = new HashMap<>();

    public static void registerInstance(HubData instance) {
        instances.put(instance.getId(), instance);
    }

    public static HubData getInstanceById(String id) {
        return instances.get(id);
    }

    public static void removeInstanceById(String id) {
        instances.remove(id);
    }

    public static Map<String, HubData> getAllInstances() {
        return instances;
    }

    public static HubData getInstanceByInstance (Instance instance) {
        for (HubData hubData : instances.values()) {
            if (hubData.getInstance().equals(instance)) {
                return hubData;
            }
        }
        return null;
    }

    public static HubData getInstanceWithLowestPlayers() {
        HubData lowest = null;
        for (HubData instance : instances.values()) {
            if (lowest == null || instance.getInstance().getPlayers().size() < lowest.getInstance().getPlayers().size()) {
                lowest = instance;
            }
        }
        return lowest;
    }

    public static HubData getInstanceWithLowestPlayersExcept (HubData hub) {
        HubData lowest = null;
        for (HubData instance : instances.values()) {
            if ((lowest == null || (instance.getInstance().getPlayers().size() < lowest.getInstance().getPlayers().size())) && !instance.equals(hub)) {
                lowest = instance;
            }
        }
        return lowest;
    }

    public static HubData getInstanceWithPlayer(UUID playerId) {
        for (HubData instance : instances.values()) {
            if (instance.getInstance().getPlayers().stream().anyMatch(player -> player.getUuid().equals(playerId))) {
                return instance;
            }
        }
        return null;
    }

    public static Collection<CustomPlayer> getAllPlayersInHubs() {
        Collection<CustomPlayer> players = new HashSet<>();
        for (HubData hubData : instances.values()) {
            players.addAll(hubData.getInstance().getPlayers().stream().map(p -> (CustomPlayer) p).toList());
        }
        return players;
    }
}
