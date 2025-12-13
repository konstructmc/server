package dev.proplayer919.konstruct.commands;

import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.matches.MatchData;
import dev.proplayer919.konstruct.matches.MatchManager;
import dev.proplayer919.konstruct.matches.MatchesRegistry;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.messages.MessageType;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;

public class JoinMatchCommand extends Command {

    public JoinMatchCommand() {
        super("join", "joinmatch", "joingame");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.SERVER, "Usage: /join <id>"));

        var idArg = ArgumentType.String("id").setSuggestionCallback((sender, context, suggestion) -> {
            for (MatchData matchData : MatchesRegistry.getMatches()) {
                suggestion.addEntry(new SuggestionEntry(matchData.getMatchUUID().toString()));
            }
        });

        addSyntax((sender, context) -> {
            final String id = context.get(idArg);
            if (sender instanceof CustomPlayer player) {
                // Prevent joining another match while already in a game
                if (MatchesRegistry.getMatchWithPlayer(player) != null) {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "You are already in a match and cannot join another.");
                    return;
                }

                // Check if the MatchData exists
                MatchData matchData = MatchesRegistry.getMatch(UUID.fromString(id));
                if (matchData != null) {
                    // Check if the match is already full
                    if (!matchData.isFull()) {
                        MatchManager.spawnPlayerIntoMatch(matchData, player);
                    } else {
                        MessagingHelper.sendMessage(player, MessageType.ERROR, "The match in instance '" + id + "' is already full. (" + matchData.getPlayerCount() + "/" + matchData.getMaxPlayers() + ")");
                    }
                } else {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "Game instance with ID '" + id + "' does not exist.");
                }
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, idArg);
    }
}