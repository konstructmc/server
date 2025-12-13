package dev.proplayer919.konstruct.messages;

import dev.proplayer919.konstruct.util.DateStringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Date;
import java.util.UUID;

public final class MatchMessages {
    private MatchMessages() {}

    public static Component createMatchAdvertiseMessage(UUID matchUUID, String hostName, Date startTime) {
        String formattedStartTime = DateStringUtility.formatDuration(startTime.getTime() - System.currentTimeMillis(), true);
        ClickEvent clickEvent = ClickEvent.runCommand("/join " + matchUUID.toString());

        return Component.text(hostName, NamedTextColor.GOLD)
                .append(Component.text(" is hosting a match, starting in ", NamedTextColor.WHITE))
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

    public static Component createLastCountdownMessage(int secondsLeft) {
        return Component.text("Match starting in ", NamedTextColor.YELLOW)
                .append(Component.text(secondsLeft + " seconds!", NamedTextColor.GOLD));
    }

    public static Component createMatchTooLittlePlayersMessage(int minPlayers) {
        return Component.text("Match cannot start: at least ", NamedTextColor.RED)
                .append(Component.text(minPlayers, NamedTextColor.GOLD))
                .append(Component.text(" players are required. You will be transferred back to a hub.", NamedTextColor.RED));
    }

    public static Component createCountdownMessage(Date startTime, int currentPlayers, int minPlayers) {
        long millisecondsLeft = startTime.getTime() - System.currentTimeMillis();
        String formattedCountdown = DateStringUtility.formatDuration(millisecondsLeft, true);
        Component message = Component.text("Match starting in ", NamedTextColor.YELLOW)
                .append(Component.text(formattedCountdown + "!", NamedTextColor.GOLD));
        int needed = Math.max(0, minPlayers - currentPlayers);
        if (needed > 0) {
            String playerWord = needed == 1 ? " player" : " players";
            message = message.append(Component.text(" We need " + needed + playerWord + " before then to start the match!", NamedTextColor.RED));
        }
        return message;
    }

    public static Component createPreMatchCountdownMessage(int secondsLeft) {
        return Component.text("Match starting in ", NamedTextColor.YELLOW)
                .append(Component.text(secondsLeft + " seconds!", NamedTextColor.GOLD));
    }

    public static Component createPlayerDisconnectMessage(String playerName, int remainingPlayers) {
        return Component.text(playerName, NamedTextColor.RED)
                .append(Component.text(" left the match and was eliminated. ", NamedTextColor.WHITE))
                .append(Component.text(remainingPlayers + " players remaining.", NamedTextColor.YELLOW));
    }

    public static Component createPlayerVoidMessage(String playerName, int remainingPlayers) {
        return Component.text(playerName, NamedTextColor.RED)
                .append(Component.text(" fell into the abyss. ", NamedTextColor.WHITE))
                .append(Component.text(remainingPlayers + " players remaining.", NamedTextColor.YELLOW));
    }

    public static Component createPlayerEliminatedMessage(String playerName, String killerName, int remainingPlayers) {
        return Component.text(playerName, NamedTextColor.RED)
                .append(Component.text(" was eliminated by ", NamedTextColor.WHITE))
                .append(Component.text(killerName, NamedTextColor.GREEN))
                .append(Component.text(". ", NamedTextColor.WHITE))
                .append(Component.text(remainingPlayers + " players remaining.", NamedTextColor.YELLOW));
    }

    public static Component createKillerMessage(String playerName) {
        return Component.text("☠ KILL ", NamedTextColor.RED)
                .append(Component.text("on ", NamedTextColor.WHITE))
                .append(Component.text(playerName, NamedTextColor.GOLD));
    }

    public static Component createWinnerMessage(String playerName) {
        return Component.text("⭐ WINNER ", NamedTextColor.GOLD)
                .append(Component.text("is ", NamedTextColor.WHITE))
                .append(Component.text(playerName, NamedTextColor.GOLD));
    }
}
