package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.feature.PickobulusPreview;
import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.AABB;

public class PickobulusPreviewContainer implements IContainer {
    public static final PickobulusPreviewContainer INSTANCE = new PickobulusPreviewContainer();
    public boolean isActivated = false;

    private String text;

    @AllArgsConstructor
    public static class State {
        public int blocks;
        public int glasses;
        public int ice;
        public AABB bounds;

        public void reset() {
            this.blocks = 0;
            this.glasses = 0;
            this.ice = 0;
            this.bounds = null;
        }
    }

    @Override
    public boolean isActive() {
        return isActivated;
    }

    @Override
    public int getLevel() {
        return ContainerLevel.COMMON;
    }

    @Override
    public void prepareRender(float scale) {
        State state = PickobulusPreview.INSTANCE.stateSlot.get();
        StringBuilder builder = new StringBuilder("Pickobulus | ");
        builder.append(state.blocks);
        builder.append(" blocks | ");
        builder.append(state.glasses);
        builder.append(" glasses | ");
        builder.append(state.ice);
        builder.append(" ice");
        text = builder.toString();
    }

    @Override
    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight;
    }

    @Override
    public float estimateWidth() {
        return 18F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(text);
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenTextCentered(font, text, left, top, right, bottom, Easy2D.TEXT_DEFAULT_COLOR, false);
    }
}
