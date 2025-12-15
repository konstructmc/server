package dev.proplayer919.konstruct.commands.admin;

import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.permissions.PlayerPermissionRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class FlyCommand extends Command {

    public FlyCommand() {
        super("fly", "flight", "togglefly", "toggleflight");

        var targetArg = ArgumentType.String("target").setDefaultValue((CommandSender sender) -> {
            if (sender instanceof CustomPlayer player) {
                return player.getUsername();
            }
            return null;
        }).setSuggestionCallback((sender, context, suggestion) -> MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> suggestion.addEntry(new SuggestionEntry(player.getUsername()))));

        addSyntax((sender, context) -> {
            final String target = context.get(targetArg);

            if (sender instanceof CustomPlayer player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.fly")) {
                    MessagingHelper.sendMessage(sender, MessageType.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                Player targetPlayer = MinecraftServer.getConnectionManager().findOnlinePlayer(target);
                if (targetPlayer == null) {
                    MessagingHelper.sendMessage(sender, MessageType.ERROR, "Player with username '" + target + "' is not online.");
                    return;
                }
                targetPlayer.setAllowFlying(targetPlayer.isAllowFlying());
                MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Toggled flight for " + target);
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, targetArg);
    }
}