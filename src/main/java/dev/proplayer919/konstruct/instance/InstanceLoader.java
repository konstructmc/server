package dev.proplayer919.konstruct.instance;

import dev.proplayer919.konstruct.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class InstanceLoader {
    public static @NotNull InstanceContainer loadAnvilInstance(String anvilPath, boolean pvpEnabled) {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        // Setup lighting
        instanceContainer.setChunkSupplier(LightingChunk::new);

        // Set Anvil loader
        instanceContainer.setChunkLoader(new AnvilLoader(anvilPath));

        // Precalculate lighting
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(instanceContainer.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            LightingChunk.relight(instanceContainer, instanceContainer.getChunks());
        });

        // Add PvP feature if enabled
        if (pvpEnabled) {
            instanceContainer.eventNode().addChild(Main.modernVanilla.createNode());
        }

        return instanceContainer;
    }
}
