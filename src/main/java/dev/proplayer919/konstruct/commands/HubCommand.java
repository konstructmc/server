package dev.proplayer919.konstruct.commands;

import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.matches.MatchesRegistry;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.hubs.HubData;
import dev.proplayer919.konstruct.hubs.HubRegistry;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.util.PlayerHubHelper;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

public class HubCommand extends Command {

    public HubCommand() {
        super("hub");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.SERVER, "Usage: /hub hub-<id>"));

        var idArg = ArgumentType.String("id").setSuggestionCallback((sender, context, suggestion) -> {
            for (HubData hubInstance : HubRegistry.getAllInstances().values()) {
                suggestion.addEntry(new SuggestionEntry(hubInstance.getId()));
            }
        });

        addSyntax((sender, context) -> {
            final String id = context.get(idArg);
            if (sender instanceof CustomPlayer player) {
                // Prevent using /hub while the player is inside a game instance
                if (MatchesRegistry.getMatchWithPlayer(player) != null) {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "You cannot use /hub while you are in a game.");
                    return;
                }

                HubData hubInstance = HubRegistry.getInstanceById(id);
                if (hubInstance != null) {
                    // If the player is already in the requested hub, do nothing
                    if (hubInstance.getPlayers().contains(player)) {
                        MessagingHelper.sendMessage(player, MessageType.SERVER, "You are already in " + id + ".");
                        return;
                    }

                    PlayerHubHelper.movePlayerToHub(player, hubInstance);
                    MessagingHelper.sendMessage(player, MessageType.SERVER, "Joined " + id + ".");
                } else {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "Hub with ID '" + id + "' does not exist.");
                }
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, idArg);
    }
}