package dev.proplayer919.konstruct.loot;

import net.minestom.server.inventory.Inventory;

import java.util.Map;

public class ChestLootRegistry {
    private final Map<String, Inventory> chests = new java.util.HashMap<>();

    public Inventory getLoot(ChestIdentifier chestId) {
        if (chests.containsKey(chestId.toString())) {
            return chests.get(chestId.toString());
        } else {
            // Generate new chest inventory
            Inventory chestInventory = LootGenerator.generateLoot(chestId);
            chests.put(chestId.toString(), chestInventory);
            return chestInventory;
        }
    }

    public void setLoot(ChestIdentifier chestId, Inventory inventory) {
        chests.put(chestId.toString(), inventory);
    }
}
