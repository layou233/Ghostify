package com.launium.ghostify.client.ui;

import com.launium.ghostify.client.mixin.AccessFont;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.Collection;

public class Easy2D {
    private static GuiGraphicsExtractor context;

    public static void configure(GuiGraphicsExtractor newContext) {
        context = newContext;
    }

    public static void cleanup() {
        context = null;
    }

    public static void drawRoundRect(float left, float top, float right, float bottom,
                                     float radius, float shadow, int color, int shadowColor) {
        if (!(left < right && top < bottom)) { // also capture NaN
            return;
        }
        context.guiRenderState.addPicturesInPictureState(new RoundRectRenderer.State(context,
                left, top, right, bottom, radius, shadow, color, shadowColor
        ));
    }

    public static final int TEXT_DEFAULT_COLOR = -1;

    public static void drawScreenText(Font font, String text, float x, float y, int color, boolean shadow) {
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(x, y);
        context.text(font, text, 0, 0, color, shadow);
        pose.popMatrix();
    }

    public static void drawScreenTextCentered(Font font, String text, float left, float top, float right, float bottom, int color, boolean shadow) {
        drawScreenText(font, text,
                (left + right - ((AccessFont) font).getSplitter().stringWidth(text)) * 0.5F,
                (top + bottom - font.lineHeight) * 0.5F,
                color, shadow);
    }

    public static void drawScreenTextAligned(Font font, String text, float left, float top, float right, float bottom, int color, boolean shadow, Alignment alignment) {
        drawScreenText(font, text,
                alignment.calculate(left, right, ((AccessFont) font).getSplitter().stringWidth(text)),
                alignment.calculate(top, bottom, font.lineHeight),
                color, shadow);
    }

    public static void drawScreenTextsCentered(Font font, float x, float y, int color, boolean shadow, Collection<String> lines) {
        if (lines.isEmpty()) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = y - font.lineHeight * lines.size() * 0.5F;
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(0f, 0f);
        int i = 0;
        for (String line : lines) {
            pose.setTranslation(x - splitter.stringWidth(line) * 0.5F, startY + font.lineHeight * i++);
            context.text(font, line, 0, 0, color, shadow);
        }
        pose.popMatrix();
    }

    public static void drawScreenTextElements(Font font, float startX, float endX, float centerY, boolean shadow, Collection<VanillaText> elements) {
        if (elements.isEmpty()) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = centerY - font.lineHeight * elements.size() * 0.5F;
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(0f, 0f);
        int i = 0;
        for (VanillaText element : elements) {
            pose.setTranslation(element.align.calculate(startX, endX, splitter.stringWidth(element.text)),
                    startY + font.lineHeight * i++);
            context.text(font, element.text, 0, 0, element.color, shadow);
        }
        pose.popMatrix();
    }

    public static void drawItem(ItemStack itemStack, float x, float y) {
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(x, y);
        context.fakeItem(itemStack, 0, 0);
        pose.popMatrix();
    }
}
