package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.config.ConfigManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class DayViewer extends AbstractModule implements ClientTickEvents.StartTick {
    public static final DayViewer INSTANCE = new DayViewer();
    private static final KeyMapping DAY_VIEWER_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.ghostify.switch_day_viewer", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, GhostifyClient.KEY_CATEGORY)
    );

    private long day = -1;
    private String formattedDay = null;

    static {
        if (ConfigManager.FEATURES.ENABLE_DAY_VIEWER) {
            GhostifyClient.moduleList.showModule(INSTANCE);
        }
    }

    @Override
    public void onStartTick(@NonNull Minecraft client) {
        while (DAY_VIEWER_KEY.consumeClick()) {
            ConfigManager.FEATURES.ENABLE_DAY_VIEWER = !ConfigManager.FEATURES.ENABLE_DAY_VIEWER;
            if (ConfigManager.FEATURES.ENABLE_DAY_VIEWER) GhostifyClient.moduleList.showModule(this);
            ConfigManager.FEATURES.markAsChanged();
        }
    }

    public void whenClientboundSetTime() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        // An in-game day lasts exactly 24,000 ticks (20 minutes)
        // See https://minecraft.wiki/w/Tick
        // The return value of getDayTime() does not change if the world
        // does not do daylight cycle, but can be changed
        // by setting time manually with "/time set" command.
        long newDay = client.level.getOverworldClockTime() / 24000L; // TODO: verify clock time
        if (newDay != day) {
            day = newDay;
            formattedDay = Long.toString(newDay);
        }
    }

    @Override
    public String title() {
        return "Day";
    }

    @Override
    public @Nullable String subtitle() {
        return formattedDay;
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_DAY_VIEWER;
    }
}
