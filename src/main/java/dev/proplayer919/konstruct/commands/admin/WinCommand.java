package dev.proplayer919.konstruct.commands.admin;

import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.matches.MatchData;
import dev.proplayer919.konstruct.matches.MatchManager;
import dev.proplayer919.konstruct.matches.MatchPlayer;
import dev.proplayer919.konstruct.matches.MatchesRegistry;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.permissions.PlayerPermissionRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

public class WinCommand extends Command {

    public WinCommand() {
        super("win", "wingame", "winmatch");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Usage: /win [target]"));

        var targetArg = ArgumentType.String("target").setDefaultValue((sender) -> {
            if (sender instanceof CustomPlayer player) {
                return player.getUsername();
            }
            return null;
        }).setSuggestionCallback((sender, context, suggestion) -> {
            if (sender instanceof CustomPlayer player) {
                MatchData matchData = MatchesRegistry.getMatchWithPlayer(player);
                if (matchData != null) {
                    for (MatchPlayer customPlayer : matchData.getPlayers()) {
                        suggestion.addEntry(new SuggestionEntry(customPlayer.getUsername()));
                    }
                }
            }
        });

        addSyntax((sender, context) -> {
            final String target = context.get(targetArg);

            if (sender instanceof CustomPlayer player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.win")) {
                    MessagingHelper.sendMessage(sender, MessageType.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                MatchData matchData = MatchesRegistry.getMatchWithPlayer(player);
                if (matchData == null) {
                    MessagingHelper.sendMessage(sender, MessageType.ERROR, "You are not currently in a match.");
                    return;
                }

                MatchPlayer targetPlayer = matchData.getPlayers().stream().filter(p -> p.getUsername().equalsIgnoreCase(target)).findFirst().orElse(null);
                if (targetPlayer == null) {
                    MessagingHelper.sendMessage(sender, MessageType.ERROR, "Player with username '" + target + "' is not online.");
                    return;
                }

                if (!matchData.isPlayerAlive(targetPlayer)) {
                    MessagingHelper.sendMessage(sender, MessageType.ERROR, "Player '" + target + "' is not in your match or is already dead.");
                    return;
                }

                // Kill all other players and declare this player the winner
                for (MatchPlayer customPlayer : matchData.getPlayers()) {
                    if (customPlayer != targetPlayer && matchData.isPlayerAlive(customPlayer)) {
                        MatchManager.killPlayer(matchData, customPlayer);
                    }
                }

                MatchManager.winMatch(matchData, targetPlayer);
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, targetArg);
    }
}