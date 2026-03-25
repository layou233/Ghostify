package com.launium.ghostify.client.ui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IContainer {
    boolean isActive();

    int getLevel();

    void prepareRender(float scale);

    float estimateHeight();

    float estimateWidth();

    void render(GuiGraphicsExtractor context, float left, float top, float right, float bottom, float scale);
}
