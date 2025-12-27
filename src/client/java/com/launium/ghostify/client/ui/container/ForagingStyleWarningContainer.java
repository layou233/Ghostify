package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ForagingStyleWarningContainer implements IContainer {
    public static final ForagingStyleWarningContainer INSTANCE = new ForagingStyleWarningContainer();
    private static final String WARNING_TEXT = "⚠ You're cutting the wrong part of the tree";

    public long lastTriggeredTimestamp = 0;

    @Override
    public boolean isActive() {
        return Util.getMillis() - lastTriggeredTimestamp < 1000L;
    }

    @Override
    public int getLevel() {
        return ContainerLevel.WARNING;
    }

    @Override
    public void prepareRender(float scale) {
    }

    @Override
    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight;
    }

    @Override
    public float estimateWidth() {
        return 18F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(WARNING_TEXT);
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenTextCentered(font, WARNING_TEXT, left, top, right, bottom, Easy2D.TEXT_DEFAULT_COLOR, false);
    }
}
