package dev.proplayer919.construkt.commands.admin;

import dev.proplayer919.construkt.messages.MessagingHelper;
import dev.proplayer919.construkt.instance.HubInstanceData;
import dev.proplayer919.construkt.instance.HubInstanceRegistry;
import dev.proplayer919.construkt.messages.Namespace;
import dev.proplayer919.construkt.permissions.PlayerPermissionRegistry;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

public class KickHubCommand extends Command {

    public KickHubCommand() {
        super("kickhub", "hubkick");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, Namespace.ADMIN, "Usage: /kickhub hub-<id>"));

        var idArg = ArgumentType.String("id");

        addSyntax((sender, context) -> {
            final String id = context.get(idArg);
            if (sender instanceof Player player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.kickhub")) {
                    MessagingHelper.sendMessage(sender, Namespace.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                HubInstanceData hubInstance = HubInstanceRegistry.getInstanceById(id);
                if (hubInstance != null) {
                    // Kick all players from the hub
                    for (var p : hubInstance.getPlayers()) {
                        // Find another hub to send them to
                        HubInstanceData targetHub = HubInstanceRegistry.getInstanceWithLowestPlayersExcept(hubInstance);
                        if (targetHub != null) {
                            p.setInstance(targetHub.getInstance());

                            p.teleport(new Pos(0.5, 40, 0.5));
                            targetHub.getPlayers().add(p);
                            MessagingHelper.sendMessage(p, Namespace.SERVER, "You have been kicked from " + id + " to " + targetHub.getId() + ".");
                        } else {
                            Component message = MessagingHelper.createMessage(Namespace.ERROR, "You have been kicked from " + id + " but no other hubs are available. Please try reconnecting later.");
                            p.kick(message);
                        }
                    }

                    MessagingHelper.sendMessage(sender, Namespace.ADMIN, "All players have been kicked from " + id + ".");
                } else {
                    MessagingHelper.sendMessage(sender, Namespace.ERROR, "Hub with ID '" + id + "' does not exist.");
                }
            }
        }, idArg);
    }
}