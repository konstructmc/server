package dev.proplayer919.konstruct.matches.modifiers;

import dev.proplayer919.konstruct.matches.MatchData;
import lombok.Getter;

@Getter
public class Modifier {
    private final String id;
    private final String name;
    private final String description;

    public Modifier(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void applyEffect(MatchData matchData) {
        // Default implementation does nothing
    }
}
