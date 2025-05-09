package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.TextElement;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import com.launium.ghostify.client.ui.island.IContainer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class SpiritPetWarningContainer implements IContainer {
    public static final SpiritPetWarningContainer instance = new SpiritPetWarningContainer();
    private static final TextElement WARNING_TEXT = new TextElement("⚠ WARNING: Spirit Pet is equipped").color(0xFFFF0000);

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
    public void prepareRender() {
    }

    @Override
    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight;
    }

    @Override
    public float estimateWidth() {
        return 18F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(WARNING_TEXT.text);
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenTextElements(font, left, right, (top + bottom) * 0.5F, false, WARNING_TEXT);
    }
}
