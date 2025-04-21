package com.launium.ghostify.client.ui.island;

import com.launium.ghostify.client.ui.Easy2D;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.HashSet;

public class HudDynamicIsland implements HudRenderCallback {

    private long lastEventTime;
    private boolean lastVisibility;

    float currentHalfWidth, currentHalfHeight;
    float targetHalfWidth = 40, targetHalfHeight = 30;

    private final HashSet<IContainer> activeContainers = HashSet.newHashSet(8);

    @Override
    public void onHudRender(GuiGraphics drawContext, DeltaTracker deltaTracker) {
        activeContainers.removeIf(container -> !container.isActive());
        if (activeContainers.isEmpty() && currentHalfWidth < 0.6F && currentHalfHeight < 0.6F) {
            lastVisibility = false;
            return;
        }
        if (!lastVisibility) { // reset size
            currentHalfWidth = 0;
            currentHalfHeight = 0;
            lastEventTime = 0;
        }
        Easy2D.configure(drawContext);
        Window window = Minecraft.getInstance().getWindow();
        float scale = (float) window.getGuiScale();
        int windowWidth = window.getGuiScaledWidth();
        long now = Util.getMillis();
        long timeDiff = now - lastEventTime;
        if (timeDiff > 40) timeDiff = 40;
        IContainer container;
        synchronized (this) {
            container = activeContainers.stream()
                    .reduce(((a, b) -> a.getLevel() > b.getLevel() ? a : b))
                    .orElse(null);
        }
        if (container != null) container.prepareRender();
        targetHalfWidth = container == null ? 0F : Math.max(24F, container.estimateWidth()) * 0.5F;
        targetHalfHeight = container == null ? 0F : Math.max(10F, container.estimateHeight()) * 0.5F;
        currentHalfWidth = Mth.clampedLerp(currentHalfWidth, targetHalfWidth, timeDiff / 180F);
        currentHalfHeight = Mth.clampedLerp(currentHalfHeight, targetHalfHeight, timeDiff / 180F);
        float left = (float) windowWidth / 2 - currentHalfWidth;
        float top = 36F;
        float right = (float) windowWidth / 2 + currentHalfWidth;
        float bottom = 36F + 2 * currentHalfHeight;
        Easy2D.drawRoundRect(left, top, right, bottom, 5, 12, 0xDB000000);
        if (container != null && currentHalfWidth / targetHalfWidth > 0.8F)
            container.render(drawContext, left, top, right, bottom, scale);
        lastEventTime = now;
        lastVisibility = true;
    }

    public void show(IContainer container) {
        if (container.isActive()) {
            synchronized (this) {
                activeContainers.add(container);
            }
        }
    }

}
