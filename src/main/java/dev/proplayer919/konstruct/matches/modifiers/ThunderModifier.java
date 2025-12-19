package dev.proplayer919.konstruct.matches.modifiers;

import dev.proplayer919.konstruct.matches.MatchData;
import net.minestom.server.instance.Weather;

public class ThunderModifier extends Modifier {
    public ThunderModifier() {
        super("thunder", "Thunderstorm", "A raging thunderstorm affects the match.");
    }

    @Override
    public void applyEffect(MatchData matchData) {
        matchData.getMatchInstance().setWeather(Weather.THUNDER);
    }
}
