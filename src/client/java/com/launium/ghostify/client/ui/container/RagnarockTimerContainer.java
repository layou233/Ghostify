package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import com.launium.ghostify.client.util.SimpleDuration;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class RagnarockTimerContainer implements IContainer {
    public static RagnarockTimerContainer INSTANCE = new RagnarockTimerContainer();

    public long lastTriggeredTimestamp = 0;
    public float gainedStrength;

    private String text;

    private long getEndTimestamp() {
        return lastTriggeredTimestamp + 10_000L;
    }

    @Override
    public boolean isActive() {
        return getEndTimestamp() > Util.getMillis();
    }

    @Override
    public int getLevel() {
        return ContainerLevel.EMERGENCY;
    }

    @Override
    public void prepareRender(float scale) {
        text = "Ragnarock: +" + gainedStrength + " ❁ Strength for " + new SimpleDuration(getEndTimestamp() - Util.getMillis()).toTimerString();
    }

    @Override
    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight;
    }

    @Override
    public float estimateWidth() {
        return 14F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(text);
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenTextCentered(font, text, left, top, right, bottom, Easy2D.TEXT_DEFAULT_COLOR, false);
    }
}
