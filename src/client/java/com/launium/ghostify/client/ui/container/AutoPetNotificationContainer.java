package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import com.launium.ghostify.client.ui.island.IContainer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class AutoPetNotificationContainer implements IContainer {
    public static final AutoPetNotificationContainer instance = new AutoPetNotificationContainer();

    public String warningText;
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

    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight;
    }

    @Override
    public float estimateWidth() {
        return 18F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(warningText);
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenText(font, warningText,
                (left + right - ((AccessFont) font).getSplitter().stringWidth(warningText)) * 0.5F,
                (top + bottom - (float) font.lineHeight) * 0.5F,
                Easy2D.TEXT_DEFAULT_COLOR, false);
    }
}
