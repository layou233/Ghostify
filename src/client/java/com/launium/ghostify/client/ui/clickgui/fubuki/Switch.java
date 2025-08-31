package com.launium.ghostify.client.ui.clickgui.fubuki;

import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.launium.ghostify.client.util.HSV;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Switch implements MeasurableElement {
    public static final float WIDTH = 15F;
    public static final float HEIGHT = 8F;
    public static final float BUTTON_RADIUS = HEIGHT * 0.9F * 0.5F;

    private static final float BUTTON_PAD = HEIGHT * 0.05F;

    private float startX, startY;
    private final int layerDepth;
    private final int colorAlpha; // do we actually need this? Anyway, :)
    private @Getter boolean enabled;
    private final HSV colorHSV;
    private final Animation percent = new Smooth(0F, 1F);
    private final @Nullable BooleanConsumer callback;

    public Switch(int color, boolean enabled, int layerDepth, @Nullable BooleanConsumer callback) {
        this.colorAlpha = ARGB.alpha(color);
        this.colorHSV = HSV.fromRGB(color);
        this.enabled = enabled;
        this.percent.target = enabled ? 1F : 0F;
        this.percent.current = this.percent.target;
        this.layerDepth = layerDepth;
        this.callback = callback;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.percent.target = enabled ? 1F : 0F;
        if (callback != null) {
            callback.accept(enabled);
        }
    }

    @Override
    public float measureHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        // tick animations
        percent.tick(timeDiff * 0.02F);

        // draw background
        int color = Mth.hsvToArgb(colorHSV.h, colorHSV.s, Math.max(colorHSV.v * percent.current, 0.3F), colorAlpha);
        Easy2D.drawRoundRect(startX, startY, startX + WIDTH, startY + HEIGHT, 4F,
                HEIGHT * 0.5F, 10F, color, 0xFFFFFFFF);

        // draw white dot
        float dotCenterX = (WIDTH - 2 * BUTTON_PAD - 2 * BUTTON_RADIUS) * percent.current;
        Easy2D.drawRoundRect(startX + dotCenterX + BUTTON_PAD, startY + BUTTON_PAD, startX + dotCenterX + BUTTON_PAD + 2 * BUTTON_RADIUS, startY + BUTTON_PAD + 2 * BUTTON_RADIUS,
                5F, BUTTON_RADIUS, 1F, 0xFFFFFFFF, 0xFFFFFFFF);
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (startX < mouseX && mouseX < startX + WIDTH && startY < mouseY && mouseY < startY + HEIGHT) {
            this.setEnabled(!this.enabled);
        }
        return false;
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }
}
