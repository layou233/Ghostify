package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.feature.RNGDrop;
import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Alignment;
import com.launium.ghostify.client.ui.CheckMarkRenderState;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.VanillaText;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import com.launium.ghostify.client.util.ChromaColor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Util;

import java.util.List;

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

    private final Animation checkIconProgress = new Smooth(0F, 1F);
    private List<VanillaText> vanillaTexts;
    private long lastTickTime;

    public void resetAnimation() {
        checkIconProgress.current = 0F;
    }

    @Override
    public void prepareRender(float scale) {
        long now = Util.getMillis();
        long timeDiff = now - lastTickTime;
        lastTickTime = now;
        if (timeDiff > 40) timeDiff = 40;
        if (Util.getMillis() - RNGDrop.INSTANCE.lastDropTimestamp > 1000L) {
            checkIconProgress.tick(timeDiff * 0.005F);
        }
        vanillaTexts = ObjectArrayList.wrap(new VanillaText[3], 0);
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
        return 18F + 4F + CheckMarkRenderState.LENGTH * 0.4F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(
                vanillaTexts.stream().map(it -> it.text).
                        reduce((a, b) -> a.length() > b.length() ? a : b).orElseThrow()
        );
    }

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        if (checkIconProgress.current > 0F) {
            context.guiRenderState.submitGuiElement(new CheckMarkRenderState(context,
                    left + 2F, (top + bottom - CheckMarkRenderState.LENGTH * 0.4F) * 0.5F,
                    0.4F, 0xFF539F54, checkIconProgress));
        }
        Easy2D.drawScreenTextElements(Minecraft.getInstance().font,
                left + 4F + CheckMarkRenderState.LENGTH * 0.4F, right, (top + bottom) * 0.5F,
                false, vanillaTexts);
        vanillaTexts.clear();
        vanillaTexts = null;
    }
}
