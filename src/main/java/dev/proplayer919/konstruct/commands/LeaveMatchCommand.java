package dev.proplayer919.konstruct.commands;

import dev.proplayer919.konstruct.instance.GameInstanceData;
import dev.proplayer919.konstruct.instance.GameInstanceRegistry;
import dev.proplayer919.konstruct.instance.HubInstanceData;
import dev.proplayer919.konstruct.instance.HubInstanceRegistry;
import dev.proplayer919.konstruct.match.MatchManager;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.util.PlayerHubHelper;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class LeaveMatchCommand extends Command {

    public LeaveMatchCommand() {
        super("leave", "leavematch", "leavegame");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> {
            // Find the GameInstanceData the player is currently in
            if (sender instanceof Player player) {
                GameInstanceData gameInstanceData = GameInstanceRegistry.getInstanceWithPlayer(player.getUuid());
                if (gameInstanceData != null) {
                    // Remove the player from the match
                    MatchManager.playerLeaveMatch(gameInstanceData, player);

                    PlayerHubHelper.returnPlayerToHub(player);
                } else {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "You are not currently in a game.");
                }
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        });
    }
}