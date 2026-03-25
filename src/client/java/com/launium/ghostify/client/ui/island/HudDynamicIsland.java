package com.launium.ghostify.client.ui.island;

import com.launium.ghostify.client.compat.SkyCubedCompat;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.container.IContainer;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

public class HudDynamicIsland implements HudElement {

    private long lastRenderTime;
    private boolean lastVisibility;

    Animation width = new Smooth(0, 80), height = new Smooth(0, 60);

    private final ObjectArraySet<IContainer> activeContainers = new ObjectArraySet<>(8);

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor drawContext, @NonNull DeltaTracker deltaTracker) {
        activeContainers.removeIf(container -> !container.isActive());
        if (activeContainers.isEmpty() && width.current < 1.2F && height.current < 1.2F) {
            lastVisibility = false;
            return;
        }
        if (!lastVisibility) { // reset size
            width.current = 0;
            height.current = 0;
            lastRenderTime = 0;
        }
        Easy2D.configure(drawContext);
        Window window = Minecraft.getInstance().getWindow();
        float scale = (float) window.getGuiScale();
        int windowWidth = window.getGuiScaledWidth();
        long now = Util.getMillis();
        long timeDiff = now - lastRenderTime;
        if (timeDiff > 40) timeDiff = 40;
        IContainer container = activeContainers.stream()
                .reduce(((a, b) -> a.getLevel() > b.getLevel() ? a : b))
                .orElse(null);
        if (container != null) container.prepareRender(scale);
        width.update(container == null ? 0F : Math.max(48F, container.estimateWidth()));
        height.update(container == null ? 0F : Math.max(20F, container.estimateHeight()));
        width.tick(timeDiff / 180F);
        height.tick(timeDiff / 180F);
        float halfWidth = 0.5F * width.current;
        float left = (float) windowWidth / 2 - halfWidth;
        float right = (float) windowWidth / 2 + halfWidth;
        float top = 36F;
        if (SkyCubedCompat.IS_EXISTS) {
            top = 50F;
        }
        float bottom = top + height.current;
        Easy2D.drawRoundRect(left, top, right, bottom, 12F, 16F, 0xDB000000, 0xDB000000);
        if (container != null) {
            // using scissor here ignores the actual rounded shape, it is just easier to implement
            // stencil buffer or depth testing might be a better alternative for this
            drawContext.scissorStack.push(new ScreenRectangle(Mth.ceil(left + 4F), Mth.ceil(top + 1F), Mth.floor(right - left - 8F), Mth.floor(bottom - top - 2F)));
            container.render(drawContext, left, top, left + width.target, bottom, scale);
            drawContext.scissorStack.pop();
        }
        Easy2D.cleanup();
        lastRenderTime = now;
        lastVisibility = true;
    }

    public void show(IContainer container) {
        if (container.isActive()) {
            activeContainers.add(container);
        }
    }

}
