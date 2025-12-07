package dev.proplayer919.konstruct.messages;

import dev.proplayer919.konstruct.util.DateStringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Date;

public final class MatchMessages {
    private MatchMessages() {}

    public static Component createMatchAdvertiseMessage(String instanceId, String hostName, String matchType, Date startTime) {
        String formattedStartTime = DateStringUtility.formatDuration(startTime.getTime() - System.currentTimeMillis());
        ClickEvent clickEvent = ClickEvent.runCommand("/join " + instanceId);

        return Component.text(hostName, NamedTextColor.GOLD)
                .append(Component.text(" is hosting a ", NamedTextColor.WHITE))
                .append(Component.text(matchType, NamedTextColor.AQUA))
                .append(Component.text(", starting in ", NamedTextColor.WHITE))
                .append(Component.text(formattedStartTime, NamedTextColor.GREEN))
                .append(Component.text("! ", NamedTextColor.WHITE))
                .append(Component.text("JOIN", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD).clickEvent(clickEvent));
    }

    public static Component createPlayerJoinedMessage(String playerName, int currentPlayers, int maxPlayers) {
        return Component.text(playerName, NamedTextColor.GREEN)
                .append(Component.text(" has joined the match! (", NamedTextColor.WHITE))
                .append(Component.text(currentPlayers, NamedTextColor.GOLD))
                .append(Component.text("/", NamedTextColor.WHITE))
                .append(Component.text(maxPlayers, NamedTextColor.GOLD))
                .append(Component.text(")", NamedTextColor.WHITE));
    }

    public static Component createPlayerLeftMessage(String playerName, int currentPlayers, int maxPlayers) {
        return Component.text(playerName, NamedTextColor.RED)
                .append(Component.text(" has left the match! (", NamedTextColor.WHITE))
                .append(Component.text(currentPlayers, NamedTextColor.GOLD))
                .append(Component.text("/", NamedTextColor.WHITE))
                .append(Component.text(maxPlayers, NamedTextColor.GOLD))
                .append(Component.text(")", NamedTextColor.WHITE));
    }

    public static Component createPlayerDisconnectMessage(String playerName) {
        return Component.text(playerName, NamedTextColor.RED)
                .append(Component.text(" disconnected.", NamedTextColor.WHITE));
    }

    public static Component createPlayerEliminatedMessage(String playerName, int remainingPlayers) {
        return Component.text(playerName, NamedTextColor.RED)
                .append(Component.text(" was eliminated! ", NamedTextColor.WHITE))
                .append(Component.text(remainingPlayers + " players remaining.", NamedTextColor.YELLOW));
    }
}
