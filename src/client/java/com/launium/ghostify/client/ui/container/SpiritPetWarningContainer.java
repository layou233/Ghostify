package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class SpiritPetWarningContainer implements IContainer {
    public static final SpiritPetWarningContainer instance = new SpiritPetWarningContainer();
    private static final String WARNING_TEXT = "⚠ WARNING: Spirit Pet is equipped";

    public long lastTriggeredTimestamp = 0;

    @Override
    public boolean isActive() {
        return Util.getMillis() - lastTriggeredTimestamp < 5000;
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
    public void render(GuiGraphicsExtractor context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenTextCentered(font, WARNING_TEXT, left, top, right, bottom, 0xFFFF0000, false);
    }
}
