package dev.proplayer919.konstruct.commands;

import dev.proplayer919.konstruct.instance.GameInstanceData;
import dev.proplayer919.konstruct.instance.GameInstanceRegistry;
import dev.proplayer919.konstruct.instance.HubInstanceData;
import dev.proplayer919.konstruct.instance.HubInstanceRegistry;
import dev.proplayer919.konstruct.match.MatchManager;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
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

                    // Find the hub with the least players and send the player there
                    HubInstanceData hubInstanceData = HubInstanceRegistry.getInstanceWithLowestPlayers();
                    if (hubInstanceData != null) {
                        hubInstanceData.getPlayers().add(player);
                        player.setInstance(hubInstanceData.getInstance());
                        player.teleport(new Pos(0.5, 40, 0.5)); // Teleport to hub spawn point
                        player.setGameMode(GameMode.SURVIVAL);
                        MessagingHelper.sendMessage(player, MessageType.SERVER, "You have been returned to the hub.");
                    } else {
                        player.kick("No hub instance available. Please try reconnecting later.");
                    }
                } else {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "You are not currently in a game.");
                }
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        });
    }
}