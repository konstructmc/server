package dev.proplayer919.konstruct.commands.admin;

import dev.proplayer919.konstruct.instance.GameInstanceData;
import dev.proplayer919.konstruct.instance.GameInstanceRegistry;
import dev.proplayer919.konstruct.instance.gameplayer.GamePlayerData;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.permissions.PlayerPermissionRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.UUID;

public class WinCommand extends Command {

    public WinCommand() {
        super("win", "wingame", "winmatch");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.win")) {
                    MessagingHelper.sendMessage(sender, MessageType.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                GameInstanceData gameInstanceData = GameInstanceRegistry.getInstanceWithPlayer(player.getUuid());
                if (gameInstanceData == null) {
                    MessagingHelper.sendMessage(sender, MessageType.ERROR, "You are not currently in a game.");
                    return;
                }

                GamePlayerData gamePlayerData = gameInstanceData.getAlivePlayers().stream()
                        .filter(gp -> gp.getUuid().equals(player.getUuid()))
                        .findFirst()
                        .orElse(null);

                // Kill all other players and declare this player the winner
                for (GamePlayerData gp : gameInstanceData.getAlivePlayers()) {
                    if (!gp.getUuid().equals(player.getUuid())) {
                        gameInstanceData.killPlayer(gp);
                    }
                }

                if (gamePlayerData != null) {
                    gameInstanceData.winMatch(gamePlayerData);
                } else {
                    MessagingHelper.sendMessage(sender, MessageType.ERROR, "You are not an active player in this game.");
                }
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        });
    }
}