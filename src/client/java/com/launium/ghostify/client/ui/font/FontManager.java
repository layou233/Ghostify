package com.launium.ghostify.client.ui.font;

import com.launium.ghostify.client.GhostifyClient;
import net.minecraft.Util;

import java.awt.*;
import java.awt.font.FontRenderContext;

public class FontManager {
    public static Font DEFAULT_FONT;
    public static Font BOLD_FONT;
    public static FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);

    public static void init() {
        try {
            String fontName;
            switch (Util.getPlatform()) {
                case WINDOWS -> fontName = "Segoe UI";
                case OSX -> fontName = "Helvetica";
                case LINUX -> fontName = "DejaVu Sans";
                default -> fontName = "Arial";
            }
            DEFAULT_FONT = new Font(fontName, Font.PLAIN, 24).deriveFont(24f);
        } catch (Exception e) {
            GhostifyClient.LOGGER.warn("Failed to load system font", e);
            // fallback to Arial and ignore further errors
            DEFAULT_FONT = new Font("Arial", Font.PLAIN, 24);
        }
        BOLD_FONT = DEFAULT_FONT.deriveFont(Font.BOLD);
    }

    public static RenderedText requestRenderedText(RenderInfo info, float scale) {
        RenderInfo scaledInfo = new RenderInfo(info.font(), info.text(), info.size() * scale);
        return RenderedTextCache.get(scaledInfo);
    }
}
