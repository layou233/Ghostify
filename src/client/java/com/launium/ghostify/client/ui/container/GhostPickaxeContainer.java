package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class GhostPickaxeContainer implements IContainer {
    public static final GhostPickaxeContainer INSTANCE = new GhostPickaxeContainer();

    public boolean isActivated;
    public boolean isLegacy;
    private static final String legacyText = "Ghost Pickaxe is activated";
    private static final String dungeonbreakerText = "Dungeonbreaker is swapped in";

    @Override
    public boolean isActive() {
        return isActivated;
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
        return 14F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(isLegacy ? legacyText : dungeonbreakerText);
    }

    @Override
    public void render(GuiGraphicsExtractor context, float left, float top, float right, float bottom, float scale) {
        Font font = Minecraft.getInstance().font;
        String text = isLegacy ? legacyText : dungeonbreakerText;
        Easy2D.drawScreenTextCentered(font, text, left, top, right, bottom, Easy2D.TEXT_DEFAULT_COLOR, false);
    }
}
