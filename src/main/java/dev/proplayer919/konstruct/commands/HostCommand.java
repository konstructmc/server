package dev.proplayer919.konstruct.commands;

import dev.proplayer919.konstruct.instance.GameInstanceData;
import dev.proplayer919.konstruct.instance.GameInstanceRegistry;
import dev.proplayer919.konstruct.instance.HubInstanceRegistry;
import dev.proplayer919.konstruct.match.MatchManager;
import dev.proplayer919.konstruct.match.types.MatchType;
import dev.proplayer919.konstruct.match.types.MatchTypeRegistry;
import dev.proplayer919.konstruct.messages.MatchMessages;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.messages.MessageType;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.Collection;
import java.util.stream.Collectors;

public class HostCommand extends Command {

    public HostCommand() {
        super("host", "hostmatch", "hostgame");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.SERVER, "Usage: /host <id>"));

        var idArg = ArgumentType.String("id");

        addSyntax((sender, context) -> {
            final String id = context.get(idArg);
            if (sender instanceof Player player) {
                // Check if the MatchType exists
                MatchType matchType = MatchTypeRegistry.getMatchTypeById(id);
                if (matchType != null) {
                    String instanceId = GameInstanceRegistry.getNextInstanceId();
                    GameInstanceData gameInstanceData = new GameInstanceData(instanceId, player.getUuid(), matchType);
                    GameInstanceRegistry.registerInstance(gameInstanceData);

                    MatchManager.spawnPlayerIntoMatch(gameInstanceData, player);

                    Collection<Audience> playersInHubs = HubInstanceRegistry.getAllPlayersInHubs()
                            .stream()
                            .map(p -> (Audience) p)
                            .collect(Collectors.toList());

                    MessagingHelper.sendMessage(playersInHubs, MatchMessages.createMatchAdvertiseMessage(gameInstanceData.getId(), player.getUsername(), matchType.getName(), gameInstanceData.getStartTime()));
                } else {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "Match type with ID '" + id + "' does not exist.");
                }
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, idArg);
    }
}