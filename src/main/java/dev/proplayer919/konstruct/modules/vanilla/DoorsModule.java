package dev.proplayer919.konstruct.modules.vanilla;

import dev.proplayer919.konstruct.CustomInstance;
import dev.proplayer919.konstruct.modules.Module;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class DoorsModule extends Module {
    private final Collection<Block> openableDoors = List.of(
            Block.ACACIA_DOOR,
            Block.BAMBOO_DOOR,
            Block.BIRCH_DOOR,
            Block.DARK_OAK_DOOR,
            Block.CHERRY_DOOR,
            Block.CRIMSON_DOOR,
            Block.JUNGLE_DOOR,
            Block.MANGROVE_DOOR,
            Block.SPRUCE_DOOR,
            Block.WARPED_DOOR,
            Block.OAK_DOOR,
            Block.COPPER_DOOR,
            Block.EXPOSED_COPPER_DOOR,
            Block.WEATHERED_COPPER_DOOR,
            Block.OXIDIZED_COPPER_DOOR,
            Block.WAXED_COPPER_DOOR,
            Block.WAXED_EXPOSED_COPPER_DOOR,
            Block.WAXED_WEATHERED_COPPER_DOOR,
            Block.WAXED_OXIDIZED_COPPER_DOOR
    );

    private final static Random random = new Random();

    private boolean isOpenableDoor(Block block) {
        return openableDoors.contains(block);
    }

    public static void setOpen(Instance instance, Point position, boolean open, boolean playEffect, Player source) {
        Block block = instance.getBlock(position);

        // Modify the half that the player clicked
        instance.setBlock(position, block.withProperty("open", Boolean.toString(open)));

        // Modify the other half
        String half = block.getProperty("half");
        Point otherHalfPos;
        if ("upper".equals(half)) {
            otherHalfPos = position.sub(0.0, 1.0, 0.0);
        } else {
            otherHalfPos = position.add(0.0, 1.0, 0.0);
        }

        instance.setBlock(otherHalfPos,
                instance.getBlock(otherHalfPos).withProperty("open", Boolean.toString(open)));

        boolean currentlyOpen = Boolean.parseBoolean(block.getProperty("open"));
        boolean shouldPlaySound = playEffect && (currentlyOpen != open);

        if (shouldPlaySound) {
            // Get material; if null, bail out
            Material material = block.registry().material();
            if (material == null) return;

            SoundPair sounds = getSounds(material);
            if (sounds == null) return;

            SoundEvent soundEvent = open ? sounds.openSound : sounds.closeSound;

            Collection<Player> audience = new ArrayList<>();
            instance.getEntityTracker().nearbyEntities(position, 16.0, EntityTracker.Target.PLAYERS, player -> {
                if (player != source) audience.add(player);
            });

            float pitch = 0.9f + random.nextFloat() * 0.1f;
            PacketGroupingAudience.of(audience).playSound(
                    Sound.sound(soundEvent, Sound.Source.BLOCK, 1.0f, pitch),
                    position
            );
        }
    }

    public static SoundPair getSounds(Material material) {
        Block block = material.registry().block();
        if (block == Block.ACACIA_DOOR
                || block == Block.BIRCH_DOOR
                || block == Block.DARK_OAK_DOOR
                || block == Block.JUNGLE_DOOR
                || block == Block.MANGROVE_DOOR
                || block == Block.SPRUCE_DOOR
                || block == Block.OAK_DOOR) {
            return new SoundPair(SoundEvent.BLOCK_WOODEN_DOOR_OPEN, SoundEvent.BLOCK_WOODEN_DOOR_CLOSE);
        }

        if (block == Block.CHERRY_DOOR) {
            return new SoundPair(SoundEvent.BLOCK_CHERRY_WOOD_DOOR_OPEN, SoundEvent.BLOCK_CHERRY_WOOD_DOOR_CLOSE);
        }

        if (block == Block.CRIMSON_DOOR || block == Block.WARPED_DOOR) {
            return new SoundPair(SoundEvent.BLOCK_NETHER_WOOD_DOOR_OPEN, SoundEvent.BLOCK_NETHER_WOOD_DOOR_CLOSE);
        }

        if (block == Block.BAMBOO_DOOR) {
            return new SoundPair(SoundEvent.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundEvent.BLOCK_BAMBOO_WOOD_DOOR_CLOSE);
        }

        if (block == Block.COPPER_DOOR
                || block == Block.EXPOSED_COPPER_DOOR
                || block == Block.WEATHERED_COPPER_DOOR
                || block == Block.OXIDIZED_COPPER_DOOR
                || block == Block.WAXED_COPPER_DOOR
                || block == Block.WAXED_EXPOSED_COPPER_DOOR
                || block == Block.WAXED_WEATHERED_COPPER_DOOR
                || block == Block.WAXED_OXIDIZED_COPPER_DOOR) {
            return new SoundPair(SoundEvent.BLOCK_COPPER_DOOR_OPEN, SoundEvent.BLOCK_COPPER_DOOR_CLOSE);
        }

        return null;
    }

    public record SoundPair(SoundEvent openSound, SoundEvent closeSound) {
    }

    @Override
    public void initialize(CustomInstance parent) {
        parent.eventNode().addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getPlayer().isSneaking() || !isOpenableDoor(event.getBlock())) {
                return;
            }

            event.setBlockingItemUse(true);

            boolean isOpen = event.getBlock().getProperty("open").equals("true");

            setOpen(event.getInstance(), event.getBlockPosition(), !isOpen, true, event.getPlayer());
        });
    }
}
