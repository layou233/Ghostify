package com.launium.ghostify.client.ui;

import com.launium.ghostify.client.mixin.AccessFont;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import org.joml.Matrix3x2fStack;

public class Easy2D {
    private static GuiGraphics context;
    private static final CachedOrthoProjectionMatrixBuffer guiProjectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("gui", 1000.0F, 11000.0F, true);

    public static void configure(GuiGraphics newContext) {
        context = newContext;
    }

    public static void cleanup() {
        context = null;
    }

    public static void drawRoundRect(float left, float top, float right, float bottom,
                                     float depth, float radius, float shadow, int color, int shadowColor) {
        if (!(left < right && top < bottom)) { // also capture NaN
            return;
        }
        context.guiRenderState.submitGuiElement(new RoundRectRenderState(context,
                left, top, right, bottom, depth, radius, shadow, color, shadowColor
        ));
    }

    public static final int TEXT_DEFAULT_COLOR = -1;

    public static void drawScreenText(Font font, String text, float x, float y, int color, boolean shadow) {
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(x, y);
        context.drawString(font, text, 0, 0, color, shadow);
        pose.popMatrix();
    }

    public static void drawScreenTextsCentered(Font font, float x, float y, int color, boolean shadow, String... lines) {
        if (lines.length == 0) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = y - font.lineHeight * lines.length * 0.5F;
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(0f, 0f);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            pose.setTranslation(x - splitter.stringWidth(line) * 0.5F, startY + font.lineHeight * i);
            context.drawString(font, line, 0, 0, color, shadow);
        }
        pose.popMatrix();
    }

    public static void drawScreenTextElements(Font font, float startX, float endX, float centerY, boolean shadow, VanillaText... elements) {
        if (elements.length == 0) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = centerY - font.lineHeight * elements.length * 0.5F;
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(0f, 0f);
        for (int i = 0; i < elements.length; i++) {
            VanillaText element = elements[i];
            pose.setTranslation(element.align.calculate(startX, endX, splitter.stringWidth(element.text)),
                    startY + font.lineHeight * i);
            context.drawString(font, element.text, 0, 0, element.color, shadow);
        }
        pose.popMatrix();
    }
}
