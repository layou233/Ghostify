package com.launium.ghostify.client.ui.container;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.island.ContainerLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.text.DecimalFormat;
import java.util.List;

public class ServerTPSContainer implements IContainer, ClientTickEvents.StartTick {
    public static final ServerTPSContainer INSTANCE = new ServerTPSContainer();
    private static final DecimalFormat TPS_FORMAT = new DecimalFormat("0.00");
    public long lastTickTimestamp;
    private long lastWorldLoad;
    private float tps = 20F;

    public void whenClientboundSetTime() {
        tps = 20_000F / (Util.getMillis() - lastTickTimestamp);
        if (tps > 20F) tps = 20F;
        if (tps < 16F) {
            GhostifyClient.island.show(this);
        }
        lastTickTimestamp = Util.getMillis();
    }

    public void whenRespawn() {
        lastWorldLoad = Util.getMillis();
        GhostifyClient.island.show(this);
    }

    @Override
    public void onStartTick(Minecraft minecraft) {
        if (Util.getMillis() - lastTickTimestamp > 2000) {
            tps = -1;
        }
    }

    @Override
    public boolean isActive() {
        return Util.getMillis() - lastWorldLoad < 8000 || tps < 16F;
    }

    @Override
    public int getLevel() {
        return ContainerLevel.COMMON;
    }

    @Override
    public void prepareRender(float scale) {
    }

    @Override
    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight * ((0F < tps && tps < 15F) ? 2 : 1);
    }

    @Override
    public float estimateWidth() {
        return 18F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(tps < 15F ? LOW_TPS_WARNING : "TPS 00.00");
    }

    private static final String LOW_TPS_WARNING = "⚠ Server may be lagging";

    @Override
    public void render(GuiGraphics context, float left, float top, float right, float bottom, float scale) {
        List<String> lines = ObjectArrayList.wrap(new String[2], 0);
        if (tps > 0) {
            lines.add("TPS " + TPS_FORMAT.format(tps));
        }
        if (tps < 15F) {
            lines.add(LOW_TPS_WARNING);
        }
        Font font = Minecraft.getInstance().font;
        Easy2D.drawScreenTextsCentered(font, (left + right) * 0.5F, (top + bottom) * 0.5F,
                Easy2D.TEXT_DEFAULT_COLOR, false, lines);
    }
}
