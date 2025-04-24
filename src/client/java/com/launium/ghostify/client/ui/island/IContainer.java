package com.launium.ghostify.client.ui.island;

import net.minecraft.client.gui.GuiGraphics;

public interface IContainer {
    boolean isActive();

    int getLevel();

    void prepareRender();

    float estimateHeight();

    float estimateWidth();

    void render(GuiGraphics context, float left, float top, float right, float bottom, float scale);
}
