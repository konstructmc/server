package dev.proplayer919.construkt.permissions;

import dev.proplayer919.construkt.storage.SqliteDatabase;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class PlayerPermissionRegistry {
    private static final SqliteDatabase db = new SqliteDatabase(java.nio.file.Path.of("data", "construkt-data.db"));

    static {
        try {
            db.connect();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // Close DB on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(db::close, "PlayerPermissionRegistry-DB-Close"));
    }

    // Grant by Permission node string
    public static void grantPermission(Player player, String permissionNode) {
        grantPermission(player.getUuid(), permissionNode);
    }

    public static void grantPermission(UUID id, String permissionNode) {
        try {
            db.insertPlayerPermissionSync(id, permissionNode);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // Check permission by node string (supports wildcard logic implemented in SqliteDatabase)
    public static boolean hasPermission(Player player, String permissionNode) {
        return hasPermission(player.getUuid(), permissionNode);
    }

    public static boolean hasPermission(UUID id, String permissionNode) {
        try {
            return db.playerHasPermissionSync(id, permissionNode);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    // Revoke by permission node string
    public static void revokePermission(Player player, String permissionNode) {
        revokePermission(player.getUuid(), permissionNode);
    }

    public static void revokePermission(UUID id, String permissionNode) {
        try {
            db.removePlayerPermissionSync(id, permissionNode);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
