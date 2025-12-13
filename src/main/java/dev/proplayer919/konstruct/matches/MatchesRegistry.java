package dev.proplayer919.konstruct.matches;

import dev.proplayer919.konstruct.CustomPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MatchesRegistry {
    private static final Map<UUID, MatchData> matches = new ConcurrentHashMap<>();

    public static void registerMatch(MatchData matchData) {
        matches.put(matchData.getMatchUUID(), matchData);
    }

    public static MatchData getMatch(UUID matchUUID) {
        return matches.get(matchUUID);
    }

    public static MatchData getMatchWithPlayer(CustomPlayer player) {
        for (MatchData matchData : matches.values()) {
            if (matchData.getPlayers().contains(player)) {
                return matchData;
            }
        }
        return null;
    }

    public static Collection<MatchData> getMatches() {
        return matches.values();
    }

    public static void unregisterMatch(UUID matchUUID) {
        matches.remove(matchUUID);
    }
}
