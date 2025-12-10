package dev.proplayer919.konstruct.commands.admin;

import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.permissions.PlayerPermissionRegistry;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Objects;
import java.util.UUID;

public class SizeCommand extends Command {

    public SizeCommand() {
        super("size", "changesize", "modifysize", "scale", "changescale", "modifyscale");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Usage: /size <size>"));

        var sizeArg = ArgumentType.Double("size");

        addSyntax((sender, context) -> {
            final double size = context.get(sizeArg);
            if (sender instanceof Player player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.size")) {
                    MessagingHelper.sendMessage(sender, MessageType.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                // Validate size
                if (size < 0.1 || size > 10.0) {
                    MessagingHelper.sendMessage(sender, MessageType.ERROR, "Size must be between 0.1 and 10.0.");
                    return;
                }

                double currentScale = player.getAttribute(Attribute.SCALE).getValue();
                AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID().toString(), size - currentScale, AttributeOperation.ADD_VALUE);
                player.getAttribute(Attribute.SCALE).addModifier(attributeModifier);
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, sizeArg);
    }
}