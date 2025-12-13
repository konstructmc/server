package dev.proplayer919.konstruct.commands;

import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.matches.MatchData;
import dev.proplayer919.konstruct.matches.MatchManager;
import dev.proplayer919.konstruct.matches.MatchesRegistry;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.util.PlayerHubHelper;
import net.minestom.server.command.builder.Command;

public class LeaveMatchCommand extends Command {

    public LeaveMatchCommand() {
        super("leave", "leavematch", "leavegame");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> {
            // Find the match the player is currently in
            if (sender instanceof CustomPlayer player) {
                MatchData matchData = MatchesRegistry.getMatchWithPlayer(player);
                if (matchData != null) {
                    // Remove the player from the match
                    MatchManager.playerLeaveMatch(matchData, player);

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