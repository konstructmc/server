package dev.proplayer919.construkt.commands;

import dev.proplayer919.construkt.messages.MessagingHelper;
import dev.proplayer919.construkt.instance.HubInstanceData;
import dev.proplayer919.construkt.instance.HubInstanceRegistry;
import dev.proplayer919.construkt.messages.Namespace;
import dev.proplayer919.construkt.sidebar.SidebarData;
import dev.proplayer919.construkt.sidebar.SidebarRegistry;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class HubCommand extends Command {

    public HubCommand() {
        super("hub");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, Namespace.SERVER, "Usage: /hub hub-<id>"));

        var idArg = ArgumentType.String("id");

        addSyntax((sender, context) -> {
            final String id = context.get(idArg);
            if (sender instanceof Player player) {
                HubInstanceData hubInstance = HubInstanceRegistry.getInstanceById(id);
                if (hubInstance != null) {
                    // Find the player's current hub, if any, and remove them from it
                    HubInstanceData currentHub = HubInstanceRegistry.getInstanceWithPlayer(player.getUuid());
                    if (currentHub != null) {
                        currentHub.getPlayers().remove(player);
                    }

                    // Update the player's sidebar
                    SidebarData sidebarData = SidebarRegistry.getSidebarByPlayerId(player.getUuid());
                    if (sidebarData != null) {
                        sidebarData.setInstanceId(hubInstance.getId());
                    }

                    // Teleport the player to the new hub
                    player.setInstance(hubInstance.getInstance());
                    player.teleport(new Pos(0.5, 40, 0.5));
                    hubInstance.getPlayers().add(player);
                    MessagingHelper.sendMessage(player, Namespace.SERVER, "Joined " + id + ".");
                } else {
                    MessagingHelper.sendMessage(player, Namespace.ERROR, "Hub with ID '" + id + "' does not exist.");
                }
            } else {
                MessagingHelper.sendMessage(sender, Namespace.ERROR, "Only players can use this command.");
            }
        }, idArg);
    }
}