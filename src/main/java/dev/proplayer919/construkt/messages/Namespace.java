package dev.proplayer919.construkt.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum Namespace {
    PROTECT("PROTECT", NamedTextColor.RED, Emojis.LOCK),
    ERROR("ERROR", NamedTextColor.RED, Emojis.CROSS_MARK),
    SERVER("SERVER", NamedTextColor.AQUA, Emojis.LIGHTNING),
    ADMIN("ADMIN", NamedTextColor.GOLD, Emojis.STAR),
    PERMISSION("PERMISSION", NamedTextColor.DARK_PURPLE, Emojis.WARNING),
    ELIMINATION("ELIMINATION", NamedTextColor.DARK_RED, Emojis.SWORD),
    BROADCAST("BROADCAST", NamedTextColor.LIGHT_PURPLE, Emojis.SPEAKER),
    SUCCESS("SUCCESS", NamedTextColor.GREEN, Emojis.CHECK_MARK),
    ANTICHEAT("ANTICHEAT", NamedTextColor.RED, Emojis.WARNING);

    private final String label;
    private final NamedTextColor color;
    private final String emoji;

    Namespace(String label, NamedTextColor color, String emoji) {
        this.label = label;
        this.color = color;
        this.emoji = emoji;
    }

    public String label() {
        return label;
    }

    public NamedTextColor color() {
        return color;
    }

    public String emoji() {
        return emoji;
    }

    public Component labelComponent() {
        String text = (emoji != null && !emoji.isEmpty()) ? emoji + " " + label : label;
        return Component.text(text).color(color);
    }
}