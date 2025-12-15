package dev.proplayer919.konstruct.commands.admin;

import dev.proplayer919.konstruct.CustomPlayer;
import dev.proplayer919.konstruct.editor.EditorSession;
import dev.proplayer919.konstruct.editor.EditorSessionRegistry;
import dev.proplayer919.konstruct.instance.InstanceLoader;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.permissions.PlayerPermissionRegistry;
import dev.proplayer919.konstruct.sidebar.SidebarData;
import dev.proplayer919.konstruct.sidebar.SidebarRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class JoinEditorSessionCommand extends Command {

    public JoinEditorSessionCommand() {
        super("joineditorsession", "joineditor", "joinbuilder");

        // Executed if no other executor can be used
        setDefaultExecutor((sender, context) -> MessagingHelper.sendMessage(sender, MessageType.ADMIN, "Usage: /joineditorsession <username>"));

        var usernameArg = ArgumentType.String("username").setSuggestionCallback((sender, context, suggestion) -> {
            EditorSessionRegistry.getPlayers().forEach(uuid -> {
                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
                if (player != null) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });
        });

        addSyntax((sender, context) -> {
            final String username = context.get("username");
            if (sender instanceof CustomPlayer player) {
                if (!PlayerPermissionRegistry.hasPermission(player, "command.editor")) {
                    MessagingHelper.sendMessage(player, MessageType.PERMISSION, "You do not have permission to use this command.");
                    return;
                }

                // Load the map
                EditorSession editorSession = EditorSessionRegistry.getSession(player.getUuid());

                if (editorSession == null) {
                    MessagingHelper.sendMessage(player, MessageType.ERROR, "No session found for player " + player.getUsername());
                    return;
                }

                editorSession.addPlayer(player);

                player.setInstance(editorSession.getInstance(), new Pos(0.5, 50, 0.5));
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);

                // Update sidebar
                SidebarData sidebarData = SidebarRegistry.getSidebarByPlayerId(player.getUuid());
                if (sidebarData != null) {
                    sidebarData.setInstanceId("In Editor");
                }
            } else {
                MessagingHelper.sendMessage(sender, MessageType.ERROR, "Only players can use this command.");
            }
        }, usernameArg);
    }
}