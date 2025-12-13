package dev.proplayer919.konstruct.util;

import net.minestom.server.coordinate.Pos;

public class PlayerSpawnHelper {
    public static Pos getPointOnCircle(Pos center, double radius, int numerator, int denominator) {
        double angleRadians = 2 * Math.PI * numerator / denominator;
        double x = center.x() + radius * Math.cos(angleRadians);
        double z = center.z() + radius * Math.sin(angleRadians);
        return center.withX(x).withZ(z);
    }

    public static Pos getSpawnPointForPlayer(int playerIndex, int playerAmount) {
        // Spread players out in a circle around the center spawn point
        Pos centerSpawn = new Pos(0.5, 40, 0.5);

        Pos point = getPointOnCircle(centerSpawn, 30, playerIndex, playerAmount);

        // Compute yaw and pitch so the player looks at the center
        double dx = centerSpawn.x() - point.x();
        double dz = centerSpawn.z() - point.z();
        double dy = centerSpawn.y() - point.y();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));

        return point.withYaw(yaw).withPitch(pitch);
    }
}
