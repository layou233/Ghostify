package com.launium.ghostify.client.util;

import net.minecraft.Util;
import net.minecraft.util.Mth;

public class FadingColor {
    public static int custom(long slow, long offset, int alpha, float hue) {
        long now = (Util.getNanos() + offset) / slow;
        float saturation = (now & 0xFFFFFFFFL) / (0xFFFFFFFFL * 0.5f) - 1f; // cast to [-1.0, 1.0]
        // period when slow=1 is about 4 seconds
        return Mth.hsvToArgb(hue, Math.abs(saturation), 1f, alpha);
    }

    public static int aqua(long slow, long offset, int alpha) {
        return custom(slow, offset, alpha, 0.53f);
    }

    public static int pink(long slow, long offset, int alpha) {
        return custom(slow, offset, alpha, 0.92f);
    }
}
