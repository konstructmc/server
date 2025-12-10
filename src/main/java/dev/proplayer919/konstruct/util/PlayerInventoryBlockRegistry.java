package dev.proplayer919.konstruct.util;

import net.minestom.server.coordinate.Pos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInventoryBlockRegistry {
    private final Map<UUID, Pos> playerInventoryBlockPositions = new HashMap<>();

    public void setPlayerInventoryBlockPosition(UUID playerId, Pos position) {
        playerInventoryBlockPositions.put(playerId, position);
    }

    public Pos getPlayerInventoryBlockPosition(UUID playerId) {
        return playerInventoryBlockPositions.get(playerId);
    }
}
