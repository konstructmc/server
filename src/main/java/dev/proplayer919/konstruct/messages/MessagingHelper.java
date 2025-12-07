package dev.proplayer919.konstruct.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collection;

public final class MessagingHelper {
    public static void sendMessage(Audience audience, MessageType messageType, String message) {
        audience.sendMessage(createMessage(messageType, message));
    }

    public static void sendMessage(Collection<Audience> audiences, MessageType messageType, String message) {
        Component component = createMessage(messageType, message);
        for (Audience audience : audiences) {
            audience.sendMessage(component);
        }
    }

    public static void sendMessage(Collection<Audience> audiences, Component message) {
        for (Audience audience : audiences) {
            audience.sendMessage(message);
        }
    }

    // Helper to build the Component for a namespace + message
    public static Component createMessage(MessageType messageType, String message) {
        return messageType.labelComponent()
                .append(Component.text(" | ").color(NamedTextColor.GRAY))
                .append(Component.text(message).color(NamedTextColor.GRAY));
    }
}
