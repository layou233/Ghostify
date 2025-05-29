package com.launium.ghostify.client.ui.island;

import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.container.IContainer;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashSet;

public class HudDynamicIsland implements HudRenderCallback {

    private long lastEventTime;
    private boolean lastVisibility;

    Animation halfWidth = new Smooth(0, 40), halfHeight = new Smooth(0, 30);

    private final HashSet<IContainer> activeContainers = HashSet.newHashSet(8);

    @Override
    public void onHudRender(GuiGraphics drawContext, DeltaTracker deltaTracker) {
        activeContainers.removeIf(container -> !container.isActive());
        if (activeContainers.isEmpty() && halfWidth.current < 0.6F && halfHeight.current < 0.6F) {
            lastVisibility = false;
            return;
        }
        if (!lastVisibility) { // reset size
            halfWidth.current = 0;
            halfHeight.current = 0;
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
        if (container != null) container.prepareRender(scale);
        halfWidth.update(container == null ? 0F : Math.max(24F, container.estimateWidth()) * 0.5F);
        halfHeight.update(container == null ? 0F : Math.max(10F, container.estimateHeight()) * 0.5F);
        halfWidth.tick(timeDiff / 180F);
        halfHeight.tick(timeDiff / 180F);
        float left = (float) windowWidth / 2 - halfWidth.current;
        float top = 36F;
        float right = (float) windowWidth / 2 + halfWidth.current;
        float bottom = 36F + 2 * halfHeight.current;
        Easy2D.drawRoundRect(left, top, right, bottom, 5, 12, 16F, 0xDB000000);
        if (container != null && halfWidth.ratio() > 0.8F)
            container.render(drawContext, left, top, right, bottom, scale);
        Easy2D.cleanup();
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
