package com.launium.ghostify.client.util;

import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class ChromaColor {
    public static int custom(long slow, long offset, int alpha, float saturation, float value) {
        long now = (Util.getNanos() + offset) / slow;
        float hue = (now & 0xFFFFFFFFL) / (0xFFFFFFFFL * 0.5f) - 1f; // cast to [-1.0, 1.0]
        // period when slow=1 is about 4 seconds
        return Mth.hsvToArgb(Math.abs(hue), saturation, value, alpha);
    }

    public static int pure(long slow, long offset, int alpha) {
        return custom(slow, offset, alpha, 1f, 1f);
    }

    public static int pale(long slow, long offset, int alpha) {
        return custom(slow, offset, alpha, 0.3f, 0.9f);
    }

    public static int dark(long slow, long offset, int alpha) {
        return custom(slow, offset, alpha, 0.9f, 0.6f);
    }
}
