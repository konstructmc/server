package dev.proplayer919.konstruct.util;

import net.minestom.server.coordinate.Pos;

public class BoundsHelper {
    public static boolean isInBounds(Pos pos, Pos bounds1, Pos bounds2) {
        double minX = Math.min(bounds1.x(), bounds2.x());
        double maxX = Math.max(bounds1.x(), bounds2.x());
        double minZ = Math.min(bounds1.z(), bounds2.z());
        double maxZ = Math.max(bounds1.z(), bounds2.z());
        double minY = Math.min(bounds1.y(), bounds2.y());
        double maxY = Math.max(bounds1.y(), bounds2.y());

        return pos.x() >= minX && pos.x() <= maxX &&
                pos.z() >= minZ && pos.z() <= maxZ &&
                pos.y() >= minY && pos.y() <= maxY;
    }
}
