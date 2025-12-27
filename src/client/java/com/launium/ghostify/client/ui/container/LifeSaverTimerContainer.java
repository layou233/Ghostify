package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.feature.LifeSaverTimer;
import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.VanillaText;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import com.launium.ghostify.client.util.SimpleDuration;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LifeSaverTimerContainer implements IContainer {
    public static final LifeSaverTimerContainer instance = new LifeSaverTimerContainer();

    @Override
    public boolean isActive() {
        if (LifeSaverTimer.INSTANCE.invincibleTicks > 0) return true;
        long now = Util.getMillis();
        for (long t : LifeSaverTimer.INSTANCE.availableTimestamp) {
            if (t > now) return true;
        }
        return false;
    }

    @Override
    public int getLevel() {
        return LifeSaverTimer.INSTANCE.invincibleTicks > 0 ? ContainerLevel.EMERGENCY : ContainerLevel.BACKGROUND;
    }

    private List<VanillaText> vanillaTexts;

    @Override
    public void prepareRender(float scale) {
        List<VanillaText> elements = ObjectArrayList.wrap(new VanillaText[1 + LifeSaverTimer.LifeSavers.values().length], 0);
        int invincibleTicks = LifeSaverTimer.INSTANCE.invincibleTicks;
        if (invincibleTicks > 0) {
            elements.add(new VanillaText(LifeSaverTimer.INSTANCE.lastTriggered.name + " lasts for " + invincibleTicks + " ticks").color(0xFFFFB4AB));
        }
        long now = Util.getMillis();
        long[] availableTimestamp = LifeSaverTimer.INSTANCE.availableTimestamp;
        IntStream.range(0, availableTimestamp.length)
                .filter(i -> availableTimestamp[i] > now)
                .mapToObj(i -> ObjectLongImmutablePair.of(LifeSaverTimer.LifeSavers.values()[i], availableTimestamp[i] - now))
                .sorted((a, b) -> Long.compare(b.rightLong(), a.rightLong()))
                .forEachOrdered(lifeSaver -> {
                    StringBuilder builder = new StringBuilder(20);
                    builder.append(lifeSaver.left().name);
                    builder.append(" in ");
                    builder.append(new SimpleDuration(lifeSaver.rightLong()).toTimerString());
                    elements.add(new VanillaText(builder.toString()));
                });
        this.vanillaTexts = elements;
    }

    @Override
    public float estimateHeight() {
        if (vanillaTexts.size() > 4) {
            System.out.println(vanillaTexts.stream().map(it -> it.text).collect(Collectors.toSet()));
        }
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
        Easy2D.drawScreenTextElements(Minecraft.getInstance().font, left, right, (top + bottom) * 0.5F,
                false, vanillaTexts);
        vanillaTexts.clear();
        vanillaTexts = null;
    }
}
