package dev.proplayer919.konstruct.matches.modifiers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModifierRegistry {
    private final static Map<String, Modifier> modifiers = new HashMap<>();

    public static void registerModifier(Modifier modifier) {
        modifiers.put(modifier.getId(), modifier);
    }

    public static Modifier getModifier(String id) {
        return modifiers.get(id);
    }

    public static Collection<Modifier> getAllModifiers() {
        return modifiers.values();
    }
}
