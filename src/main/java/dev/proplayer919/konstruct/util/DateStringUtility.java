package dev.proplayer919.konstruct.util;

public class DateStringUtility {
    public static String formatDuration(long millis) {
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

    public static String formatDuration(long millis, boolean round) {
        long seconds = millis / 1000;
        if (round) {
            long rem = seconds % 60;
            if (rem >= 30) seconds += 60 - rem;
            else seconds -= rem;
        }
        long days = seconds / 86_400; seconds %= 86_400;
        long hours = seconds / 3_600; seconds %= 3_600;
        long minutes = seconds / 60; seconds %= 60;

        StringBuilder sb = new StringBuilder();
        boolean has = false;
        if (days > 0) { sb.append(days).append("d"); has = true; }
        if (hours > 0) { if (has) sb.append(' '); sb.append(hours).append("h"); has = true; }
        if (minutes > 0) { if (has) sb.append(' '); sb.append(minutes).append("m"); has = true; }
        if (seconds > 0 || !has) { if (has) sb.append(' '); sb.append(seconds).append("s"); }
        return sb.toString();
    }
}
