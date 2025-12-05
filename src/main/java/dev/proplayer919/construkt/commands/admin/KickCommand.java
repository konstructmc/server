package dev.proplayer919.construkt.commands.admin;

import dev.proplayer919.construkt.messages.MessagingHelper;
import dev.proplayer919.construkt.messages.Namespace;
import dev.proplayer919.construkt.permissions.PlayerPermissionRegistry;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick");

        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, Namespace.ADMIN, "Usage: /kick <player> [message]"));

        var playerArg = ArgumentType.String("player");
        var messageArg = ArgumentType.StringArray("message").setDefaultValue(new String[0]);

        addSyntax((sender, context) -> {
            if (!PlayerPermissionRegistry.hasPermission(sender instanceof Player ? (Player) sender : null, "command.kick")) {
                MessagingHelper.sendMessage(sender, Namespace.PERMISSION, "You do not have permission to use this command.");
                return;
            }

            String targetName = context.get(playerArg);
            String[] msgParts = context.get(messageArg);
            String message = msgParts.length > 0 ? String.join(" ", msgParts) : "You have been kicked from the server.";

            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
            if (target == null) {
                MessagingHelper.sendMessage(sender, Namespace.ERROR, "Player '" + targetName + "' not found.");
                return;
            }

            Component comp = MessagingHelper.createMessage(Namespace.ADMIN, message);
            target.kick(comp);
            MessagingHelper.sendMessage(sender, Namespace.ADMIN, "Kicked " + targetName + ".");
        }, playerArg, messageArg);
    }
}

