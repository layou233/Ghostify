package com.launium.ghostify.client.util;

import org.jetbrains.annotations.NotNull;

public record SimpleDuration(long millis) {
    public SimpleDuration truncatedToSeconds() {
        return new SimpleDuration((millis / 1000L) * 1000L);
    }

    public @NotNull String toString() {
        long t = millis;
        long ms = t % 1000L;
        long s = (t /= 1000L) % 60L;
        long m = (t /= 60L) % 60L;
        long h = (t /= 60L) % 24L;
        long d = t / 24L;
        StringBuilder builder = new StringBuilder();
        if (d > 0) {
            builder.append(d);
            builder.append("d");
        }
        if (h > 0) {
            builder.append(h);
            builder.append("h");
        }
        if (m > 0) {
            builder.append(m);
            builder.append("m");
        }
        if (s > 0) {
            builder.append(s);
            builder.append("s");
        }
        if (ms > 0) {
            builder.append(ms);
            builder.append("ms");
        }
        return builder.isEmpty() ? "NOW" : builder.toString();
    }

    public @NotNull String toTimerString() {
        return millis < 1000L ? millis + "ms" : millis / 1000L + "s";
    }
}
