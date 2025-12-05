package dev.proplayer919.construkt.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Date;

public final class BanMessage {
    private BanMessage() {}

    public static Component buildBanComponent(String reason, Long expiresAtMillis) {
        Component header = Component.text("YOU ARE BANNED").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        Component reasonComp = Component.text("Reason: ").color(NamedTextColor.GRAY).append(Component.text(reason != null ? reason : "Banned by an operator.").color(NamedTextColor.WHITE));

        Component expiryComp;
        if (expiresAtMillis == null || expiresAtMillis <= 0) {
            expiryComp = Component.text("Duration: Permanent").color(NamedTextColor.GRAY);
        } else {
            long now = System.currentTimeMillis();
            long remaining = expiresAtMillis - now;
            if (remaining < 0) remaining = 0;
            String remainingStr = formatDuration(remaining);
            expiryComp = Component.text("Remaining: ").color(NamedTextColor.GRAY)
                    .append(Component.text(remainingStr).color(NamedTextColor.WHITE))
                    .append(Component.text("\nExpires: ").color(NamedTextColor.GRAY))
                    .append(Component.text(new Date(expiresAtMillis).toString()).color(NamedTextColor.WHITE));
        }

        return header
                .append(Component.text("\n").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                .append(reasonComp.decoration(TextDecoration.BOLD, false))
                .append(Component.text("\n").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                .append(expiryComp.decoration(TextDecoration.BOLD, false));
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86_400; seconds %= 86_400;
        long hours = seconds / 3_600; seconds %= 3_600;
        long minutes = seconds / 60; seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
