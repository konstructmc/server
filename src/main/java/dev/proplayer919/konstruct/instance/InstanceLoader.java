package dev.proplayer919.konstruct.instance;

import dev.proplayer919.konstruct.CustomInstance;
import dev.proplayer919.konstruct.Main;
import dev.proplayer919.konstruct.modules.combat.ModernCombatModule;
import dev.proplayer919.konstruct.modules.gameplay.VoidBackModule;
import dev.proplayer919.konstruct.modules.vanilla.DoorsModule;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InstanceLoader {
    public static @NotNull CustomInstance loadAnvilInstance(String anvilPath) {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        CustomInstance instance = new CustomInstance(UUID.randomUUID(), DimensionType.OVERWORLD);
        instanceManager.registerInstance(instance);

        // Setup lighting
        instance.setChunkSupplier(LightingChunk::new);

        // Set Anvil loader
        instance.setChunkLoader(new AnvilLoader(anvilPath));

        // Precalculate lighting
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(instance.loadChunk(x, z)));
        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            LightingChunk.relight(instance, instance.getChunks());
        });

        return instance;
    }

    public static @NotNull CustomInstance loadHubInstance() {
        CustomInstance instance = loadAnvilInstance("data/maps/hub");
        instance.addModule(new VoidBackModule());
        return instance;
    }

    public static @NotNull CustomInstance loadLobbyInstance() {
        CustomInstance instance = loadAnvilInstance("data/maps/lobby");
        instance.addModule(new VoidBackModule());
        return instance;
    }

    public static @NotNull CustomInstance loadDeathmatchInstance() {
        CustomInstance instance = loadAnvilInstance("data/maps/arenas/deathmatch1");
        instance.addModule(new ModernCombatModule());
        instance.addModule(new DoorsModule());
        return instance;
    }
}
