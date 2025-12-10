package dev.proplayer919.konstruct.loot;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.*;

public class LootGenerator {
    public static Inventory generateLoot(ChestIdentifier chestId) {
        Block block = chestId.block();

        if (block.name().equals("minecraft:chest")) {
            return generateT1Loot();
        } else if (block.name().equals("minecraft:ender_chest")) {
            return generateT2Loot();
        }

        throw new IllegalArgumentException("Unsupported chest block type for loot generation: " + block);
    }

    private static Inventory generateT1Loot() {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Tier 1 Chest"));

        Random rnd = new Random();
        List<Material> weighted = new ArrayList<>();

        // Wooden tools (common)
        for (int i = 0; i < 6; i++) {
            weighted.add(net.minestom.server.item.Material.WOODEN_SWORD);
            weighted.add(net.minestom.server.item.Material.WOODEN_PICKAXE);
            weighted.add(net.minestom.server.item.Material.WOODEN_AXE);
            weighted.add(net.minestom.server.item.Material.WOODEN_SHOVEL);
        }

        // Stone tools (rarer)
        for (int i = 0; i < 2; i++) {
            weighted.add(Material.STONE_SWORD);
            weighted.add(Material.STONE_PICKAXE);
            weighted.add(Material.STONE_AXE);
            weighted.add(Material.STONE_SHOVEL);
        }

        // Leather armor (uncommon)
        for (int i = 0; i < 3; i++) {
            weighted.add(Material.LEATHER_HELMET);
            weighted.add(Material.LEATHER_CHESTPLATE);
            weighted.add(Material.LEATHER_LEGGINGS);
            weighted.add(Material.LEATHER_BOOTS);
        }

        // Chainmail armor (rare)
        for (int i = 0; i < 1; i++) {
            weighted.add(Material.CHAINMAIL_HELMET);
            weighted.add(Material.CHAINMAIL_CHESTPLATE);
            weighted.add(Material.CHAINMAIL_LEGGINGS);
            weighted.add(Material.CHAINMAIL_BOOTS);
        }

        // Stackables / consumables (varying weights)
        for (int i = 0; i < 10; i++) weighted.add(Material.OAK_PLANKS);
        for (int i = 0; i < 8; i++) weighted.add(Material.COBBLESTONE);
        for (int i = 0; i < 6; i++) weighted.add(Material.BREAD);
        for (int i = 0; i < 5; i++) weighted.add(Material.COOKED_BEEF);
        for (int i = 0; i < 12; i++) weighted.add(Material.STICK);
        for (int i = 0; i < 1; i++) weighted.add(Material.SHIELD);

        int itemsToPlace = 3 + rnd.nextInt(7); // 3-9 items
        Set<Integer> usedSlots = new HashSet<>();

        return chooseLoot(weighted, rnd, itemsToPlace, inventory, usedSlots);
    }

    private static Inventory generateT2Loot() {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Tier 2 Chest"));

        Random rnd = new Random();
        List<Material> weighted = new ArrayList<>();

        // Stone tools (rarer)
        for (int i = 0; i < 2; i++) {
            weighted.add(Material.STONE_SWORD);
            weighted.add(Material.STONE_PICKAXE);
            weighted.add(Material.STONE_AXE);
            weighted.add(Material.STONE_SHOVEL);
        }

        // Iron tools (common)
        for (int i = 0; i < 5; i++) {
            weighted.add(Material.IRON_SWORD);
            weighted.add(Material.IRON_PICKAXE);
            weighted.add(Material.IRON_AXE);
            weighted.add(Material.IRON_SHOVEL);
        }

        // Leather armor (uncommon)
        for (int i = 0; i < 3; i++) {
            weighted.add(Material.LEATHER_HELMET);
            weighted.add(Material.LEATHER_CHESTPLATE);
            weighted.add(Material.LEATHER_LEGGINGS);
            weighted.add(Material.LEATHER_BOOTS);
        }

        // Chainmail armor (common)
        for (int i = 0; i < 4; i++) {
            weighted.add(Material.CHAINMAIL_HELMET);
            weighted.add(Material.CHAINMAIL_CHESTPLATE);
            weighted.add(Material.CHAINMAIL_LEGGINGS);
            weighted.add(Material.CHAINMAIL_BOOTS);
        }

        // Iron armor (rare)
        for (int i = 0; i < 2; i++) {
            weighted.add(Material.IRON_HELMET);
            weighted.add(Material.IRON_CHESTPLATE);
            weighted.add(Material.IRON_LEGGINGS);
            weighted.add(Material.IRON_BOOTS);
        }

        // Buckets
        for (int i = 0; i < 2; i++) weighted.add(Material.WATER_BUCKET);
        for (int i = 0; i < 1; i++) weighted.add(Material.LAVA_BUCKET);

        // Stackables / consumables (varying weights)
        for (int i = 0; i < 8; i++) weighted.add(Material.COBBLESTONE);
        for (int i = 0; i < 6; i++) weighted.add(Material.GOLDEN_CARROT);
        for (int i = 0; i < 7; i++) weighted.add(Material.COOKED_BEEF);
        for (int i = 0; i < 3; i++) weighted.add(Material.SHIELD);

        int itemsToPlace = 6 + rnd.nextInt(11); // 6-16 items
        Set<Integer> usedSlots = new HashSet<>();

        return chooseLoot(weighted, rnd, itemsToPlace, inventory, usedSlots);
    }

    private static Inventory chooseLoot(List<Material> weighted, Random rnd, int itemsToPlace, Inventory inventory, Set<Integer> usedSlots) {
        int placed = 0;
        int slotCount = InventoryType.CHEST_3_ROW.getSize(); // 27

        while (placed < itemsToPlace && placed < slotCount) {
            int slot = rnd.nextInt(slotCount);
            if (usedSlots.contains(slot)) continue;

            Material mat = weighted.get(rnd.nextInt(weighted.size()));
            int count = 1;
            // Determine stack sizes for stackable items
            if (mat == Material.OAK_PLANKS || mat == Material.COBBLESTONE) {
                count = 4 + rnd.nextInt(13); // 4-16
            } else if (mat == Material.BREAD || mat == Material.COOKED_BEEF || mat == Material.GOLDEN_CARROT) {
                count = 1 + rnd.nextInt(4); // 1-4
            } else if (mat == Material.STICK) {
                count = 1 + rnd.nextInt(8); // 1-8
            } else {
                // tools / non-stackables
            }

            ItemStack itemStack = ItemStack.of(mat, count);

            inventory.setItemStack(slot, itemStack);
            usedSlots.add(slot);
            placed++;
        }

        return inventory;
    }
}
