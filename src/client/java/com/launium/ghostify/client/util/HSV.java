package com.launium.ghostify.client.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.ARGB;

@Data
@AllArgsConstructor
public class HSV {
    /**
     * hue
     */
    public float h;

    /**
     * saturation
     */
    public float s;

    /**
     * value
     */
    public float v;

    public static HSV fromRGB(int color) {
        // R, G, B values are divided by 255
        // to change the range from 0..255 to 0..1
        float r = ARGB.red(color) / 255F;
        float g = ARGB.green(color) / 255F;
        float b = ARGB.blue(color) / 255F;

        // Compute min and max values
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        // Compute Saturation
        float s = (max == 0f) ? 0f : (delta / max);

        // Compute Hue
        float h;
        if (delta == 0f) {
            h = 0f;
        } else if (max == r) {
            h = ((g - b) / delta) % 6f;
        } else if (max == g) {
            h = ((b - r) / delta) + 2f;
        } else {
            h = ((r - g) / delta) + 4f;
        }

        // Normalize Hue to [0, 1)
        h /= 6f;
        if (h < 0f) {
            h += 1f;
        }

        return new HSV(h, s, max);
    }
}
