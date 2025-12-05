package dev.proplayer919.construkt.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("unused")
public final class MessagingHelper {
    public static void sendMessage(Audience audience, Namespace namespace, String message) {
        audience.sendMessage(createMessage(namespace, message));
    }

    // Helper to build the Component for a namespace + message
    public static Component createMessage(Namespace namespace, String message) {
        return namespace.labelComponent()
                .append(Component.text(" | ").color(NamedTextColor.GRAY))
                .append(Component.text(message).color(NamedTextColor.GRAY));
    }
}
