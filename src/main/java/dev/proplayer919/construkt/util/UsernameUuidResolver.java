package dev.proplayer919.construkt.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UsernameUuidResolver {
    private UsernameUuidResolver() {}

    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final long TTL_MILLIS = 10 * 60 * 1000L; // 10 minutes

    // Returns UUID if found, otherwise null
    public static UUID resolveUuid(String username) {
        if (username == null || username.isEmpty()) return null;
        String key = username.toLowerCase(java.util.Locale.ROOT);
        CacheEntry cached = CACHE.get(key);
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.timestamp <= TTL_MILLIS) {
            return cached.uuid;
        }
        try {
            String api = "https://api.mojang.com/users/profiles/minecraft/" + username;
            URL url = new URL(api);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            int code = con.getResponseCode();
            if (code != 200) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                String body = sb.toString();
                // body looks like: {"id":"<hexid>","name":"username"}
                int idIndex = body.indexOf("\"id\"");
                if (idIndex == -1) return null;
                int colon = body.indexOf(':', idIndex);
                if (colon == -1) return null;
                int quote1 = body.indexOf('"', colon);
                int quote2 = body.indexOf('"', quote1 + 1);
                if (quote1 == -1 || quote2 == -1) return null;
                String id = body.substring(quote1 + 1, quote2);
                if (id.length() != 32) return null;
                // insert dashes into id to make standard UUID string
                String uuidStr = id.substring(0,8) + "-" + id.substring(8,12) + "-" + id.substring(12,16) + "-" + id.substring(16,20) + "-" + id.substring(20);
                UUID uuid = UUID.fromString(uuidStr);
                CACHE.put(key, new CacheEntry(uuid, now));
                return uuid;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static class CacheEntry {
        final UUID uuid;
        final long timestamp;
        CacheEntry(UUID uuid, long timestamp) { this.uuid = uuid; this.timestamp = timestamp; }
    }
}
