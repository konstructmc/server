package dev.proplayer919.konstruct.loot;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public record ChestIdentifier(Block block, Pos chestPos) {

    public @NotNull String toString() {
        return "ChestIdentifier{" +
                "block=" + block +
                ", chestPos=" + chestPos +
                '}';
    }
}
