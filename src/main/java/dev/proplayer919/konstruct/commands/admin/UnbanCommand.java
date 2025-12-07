package dev.proplayer919.konstruct.commands.admin;

import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.permissions.PlayerPermissionRegistry;
import dev.proplayer919.konstruct.storage.SqliteDatabase;
import dev.proplayer919.konstruct.util.UsernameUuidResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class UnbanCommand extends Command {
    private static final SqliteDatabase db = new SqliteDatabase(java.nio.file.Path.of("data", "konstruct-data.db"));

    static {
        try {
            db.connect();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(db::close, "UnbanCommand-DB-Close"));
    }

    public UnbanCommand() {
        super("unban");

        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Usage: /unban <player>"));

        var playerArg = ArgumentType.String("player");

        addSyntax((sender, context) -> {
            // Only check permissions for actual player senders. Allow console/non-player to run the command.
            if (sender instanceof Player) {
                if (!PlayerPermissionRegistry.hasPermission((Player) sender, "command.unban")) {
                    MessagingHelper.sendMessage(sender, MessageType.PERMISSION, "You do not have permission to use this command.");
                    return;
                }
            }

            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
            UUID targetUuid = null;
            if (target != null) targetUuid = target.getUuid();

            try {
                boolean removedAny = false;
                if (targetUuid != null) {
                    if (db.isPlayerBannedSync(targetUuid)) {
                        db.removeBanSync(targetUuid);
                        removedAny = true;
                    }
                } else {
                    // Offline: attempt to resolve name -> UUID via Mojang API and require it
                    UUID resolved = UsernameUuidResolver.resolveUuid(targetName);
                    if (resolved == null) {
                        MessagingHelper.sendMessage(sender, MessageType.ERROR, "Failed to resolve username to UUID; cannot unban offline player. Ensure the username is correct and Mojang API is reachable.");
                        return;
                    }
                    if (db.isPlayerBannedSync(resolved)) {
                        db.removeBanSync(resolved);
                        removedAny = true;
                    }
                }
                if (!removedAny) {
                    MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Player " + targetName + " is not banned.");
                    return;
                }
            } catch (Throwable t) {
                t.printStackTrace();
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Failed to remove ban from database.");
                return;
            }

            MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Unbanned " + targetName + ".");
        }, playerArg);
    }
}
