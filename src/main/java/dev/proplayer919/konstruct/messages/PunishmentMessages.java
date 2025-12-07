package dev.proplayer919.konstruct.messages;

import dev.proplayer919.konstruct.util.DateStringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Date;

public final class PunishmentMessages {
    private PunishmentMessages() {}

    public static Component buildBanComponent(String reason, Long expiresAtMillis) {
        Component header = Component.text("YOU ARE BANNED").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        Component reasonComp = Component.text("Reason: ").color(NamedTextColor.GRAY).append(Component.text(reason != null ? reason : "Banned by an operator.").color(NamedTextColor.WHITE));

        Component expiryComp;
        if (expiresAtMillis == null || expiresAtMillis <= 0) {
            expiryComp = Component.text("Duration: ").color(NamedTextColor.GRAY)
                    .append(Component.text("Permanent").color(NamedTextColor.WHITE));
        } else {
            long now = System.currentTimeMillis();
            long remaining = expiresAtMillis - now;
            if (remaining < 0) remaining = 0;
            String remainingStr = DateStringUtility.formatDuration(remaining);
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

    public static Component buildKickComponent(String reason) {
        Component header = Component.text("YOU HAVE BEEN KICKED").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        Component reasonComp = Component.text("Reason: ").color(NamedTextColor.GRAY).append(Component.text(reason != null ? reason : "Banned by an operator.").color(NamedTextColor.WHITE));

        return header
                .append(Component.text("\n").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                .append(reasonComp.decoration(TextDecoration.BOLD, false))
                .append(Component.text("\n").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false));
    }
}
