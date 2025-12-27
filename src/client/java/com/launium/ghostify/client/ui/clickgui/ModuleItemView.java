package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.ui.clickgui.fubuki.Switch;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.font.RenderInfo;
import com.launium.ghostify.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleItemView implements MeasurableElement {
    public static final int LAYER_DEPTH = ClickGUIScreen.LAYER_DEPTH + 3;
    public static final float HEIGHT = 14F;

    public @NotNull String title;
    public @Nullable String subtitle;

    private float startX, startY, endX, endY;
    private Window window;
    private Runnable callback;
    private Switch simpleSwitcher;

    private ModuleItemView(Minecraft client, @NotNull String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        this.window = client.getWindow();
    }

    ModuleItemView(Minecraft client, @NotNull String title, String subtitle, boolean switcherValue, @NotNull BooleanConsumer simpleSwitcherCallback) {
        this(client, title, subtitle);
        this.simpleSwitcher = new Switch(0xFF004CFF, switcherValue, LAYER_DEPTH + 1, simpleSwitcherCallback);
    }

    ModuleItemView(Minecraft client, @NotNull String title, String subtitle, @NotNull Runnable callback) {
        this(client, title, subtitle);
        this.callback = callback;
    }

    @Override
    public float measureHeight() {
        return HEIGHT;
    }

    @Override
    public int getLayerDepth() {
        return LAYER_DEPTH;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        RenderedText titleText = FontManager.requestRenderedText(
                new RenderInfo(FontManager.DEFAULT_FONT, title, 8F), scale);
        titleText.draw(context, startX, startY, scale, 0xFFFFFFFF);
        if (subtitle != null) {
            RenderedText subtitleText = FontManager.requestRenderedText(
                    new RenderInfo(FontManager.DEFAULT_FONT, subtitle, 6F), scale);
            subtitleText.draw(context, startX, startY + 8F, scale, 0xFFFFFFFF);
        }

        if (simpleSwitcher != null) {
            float offsetY = 0.5F * (HEIGHT - Switch.HEIGHT);
            simpleSwitcher.updateStartPosition(endX - Switch.WIDTH - 2F, startY + offsetY);
            simpleSwitcher.updateEndPosition(endX - 2F, endY - offsetY);
            simpleSwitcher.render(context, mouseX, mouseY, timeDiff);
        }
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
        this.endX = newX;
        this.endY = newY;
    }

    @Override
    public void remove() {
        MeasurableElement.super.remove();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (callback != null) {
            callback.run();
        } else if (simpleSwitcher != null) {
            simpleSwitcher.mouseClicked(mouseX, mouseY);
        } else {
            return false; // but why?
        }
        return true;
    }
}
