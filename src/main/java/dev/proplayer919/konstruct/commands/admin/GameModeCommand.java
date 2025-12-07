package dev.proplayer919.konstruct.commands.admin;

import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.permissions.PlayerPermissionRegistry;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GameModeCommand extends Command {

    public GameModeCommand() {
        super("gamemode", "gm", "setgamemode", "setgm");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Usage: /gamemode <gamemode>"));

        var gamemodeArg = ArgumentType.String("gamemode");

        addSyntax((sender, context) -> {
            final String gamemode = context.get(gamemodeArg);
            if (sender instanceof Player player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.gamemode")) {
                    MessagingHelper.sendMessage(sender, MessageType.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                switch (gamemode.toLowerCase()) {
                    case "survival", "s", "0" -> player.setGameMode(GameMode.SURVIVAL);
                    case "creative", "c", "1" -> player.setGameMode(GameMode.CREATIVE);
                    case "adventure", "a", "2" -> player.setGameMode(GameMode.ADVENTURE);
                    case "spectator", "sp", "3" -> player.setGameMode(GameMode.SPECTATOR);
                    default -> {
                        MessagingHelper.sendMessage(sender, MessageType.ERROR, "Invalid gamemode '" + gamemode + "'. Valid options are: survival, creative, adventure, spectator.");
                        return;
                    }
                }

                MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Your gamemode has been set to " + gamemode + ".");
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, gamemodeArg);
    }
}