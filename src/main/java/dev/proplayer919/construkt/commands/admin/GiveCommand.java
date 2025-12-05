package dev.proplayer919.construkt.commands.admin;

import dev.proplayer919.construkt.messages.MessagingHelper;
import dev.proplayer919.construkt.messages.Namespace;
import dev.proplayer919.construkt.permissions.PlayerPermissionRegistry;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Objects;

public class GiveCommand extends Command {

    public GiveCommand() {
        super("give", "giveme", "getitem", "get", "i", "item", "giveitem");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, Namespace.ADMIN, "Usage: /give <item> [amount]"));

        var itemIdArg = ArgumentType.String("item-id");
        var amountArg = ArgumentType.Integer("amount").setDefaultValue(1);

        addSyntax((sender, context) -> {
            final String itemId = context.get(itemIdArg);
            final int amount = context.get(amountArg);
            if (sender instanceof Player player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.gamemode")) {
                    MessagingHelper.sendMessage(sender, Namespace.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                try {
                    ItemStack itemStack = ItemStack.of(Objects.requireNonNull(Material.fromKey(itemId)), amount);
                    player.getInventory().addItemStack(itemStack);
                    MessagingHelper.sendMessage(sender, Namespace.ADMIN, "Gave you " + amount + " of item " + itemId);
                } catch (Exception e) {
                    MessagingHelper.sendMessage(sender, Namespace.ERROR, "Invalid item ID: " + itemId);
                }
            } else {
                MessagingHelper.sendMessage(sender, Namespace.ERROR, "Only players can use this command.");
            }
        }, itemIdArg, amountArg);
    }
}