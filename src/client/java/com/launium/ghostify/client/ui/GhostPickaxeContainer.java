package com.launium.ghostify.client.ui;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import com.launium.ghostify.client.ui.island.IContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class GhostPickaxeContainer implements IContainer {
    public boolean isActivated;
    private static final String text = "Ghost Pickaxe is activated";

    @Override
    public boolean isActive() {
        return isActivated;
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
        return 14F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(text);
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenText(font, text,
                (left + right - ((AccessFont) font).getSplitter().stringWidth(text)) * 0.5F,
                (top + bottom - (float) font.lineHeight) * 0.5F,
                Easy2D.TEXT_DEFAULT_COLOR, false);
    }
}
