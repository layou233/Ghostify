package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.feature.RNGDrop;
import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Alignment;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.VanillaText;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import com.launium.ghostify.client.util.ChromaColor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;

public class RNGDropContainer implements IContainer {
    public static final RNGDropContainer INSTANCE = new RNGDropContainer();

    @Override
    public boolean isActive() {
        return Util.getMillis() - RNGDrop.INSTANCE.lastDropTimestamp < 10000L;
    }

    @Override
    public int getLevel() {
        return ContainerLevel.EMERGENCY;
    }

    private ArrayList<VanillaText> vanillaTexts;

    @Override
    public void prepareRender(float scale) {
        vanillaTexts = new ArrayList<>(3);
        vanillaTexts.add(new VanillaText("🎉 RNG DROP CLAIMED, GG!").color(ChromaColor.pale(2L, 0L, 0xFF)));
        vanillaTexts.add(new VanillaText("> " + RNGDrop.INSTANCE.itemName).startFrom(Alignment.START));
        if (RNGDrop.INSTANCE.sourceName != null) {
            vanillaTexts.add(new VanillaText("@ " + RNGDrop.INSTANCE.sourceName).startFrom(Alignment.START));
        }
    }

    @Override
    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight * vanillaTexts.size();
    }

    @Override
    public float estimateWidth() {
        return 18F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(
                vanillaTexts.stream().map(it -> it.text).
                        reduce((a, b) -> a.length() > b.length() ? a : b).orElseThrow()
        );
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        Easy2D.drawScreenTextElements(Minecraft.getInstance().font,
                left, right, (top + bottom) * 0.5F,
                false, vanillaTexts.toArray(new VanillaText[0]));
        vanillaTexts.clear();
        vanillaTexts = null;
    }
}
