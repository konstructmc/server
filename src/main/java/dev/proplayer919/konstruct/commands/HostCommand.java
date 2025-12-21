package dev.proplayer919.konstruct.commands;

import dev.proplayer919.konstruct.CustomInstance;
import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.instance.InstanceLoader;
import dev.proplayer919.konstruct.matches.MatchData;
import dev.proplayer919.konstruct.matches.MatchManager;
import dev.proplayer919.konstruct.matches.MatchesRegistry;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.messages.MessageType;
import net.minestom.server.command.builder.Command;

public class HostCommand extends Command {

    public HostCommand() {
        super("host", "hostmatch", "hostgame");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> {
            if (sender instanceof CustomPlayer player) {
                CustomInstance lobbyInstance = InstanceLoader.loadLobbyInstance();
                CustomInstance matchInstance = InstanceLoader.loadDeathmatchInstance();
                MatchData matchData = new MatchData(player, lobbyInstance, matchInstance);
                MatchesRegistry.registerMatch(matchData);
                MatchManager.setupMatch(matchData);
                MatchManager.spawnPlayerIntoMatch(matchData, player);
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        });
    }
}