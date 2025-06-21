package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class DayViewer extends AbstractModule implements ClientTickEvents.StartTick {
    public static final DayViewer INSTANCE = new DayViewer();
    private static final KeyMapping DAY_VIEWER_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.switch_day_viewer", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, GhostifyClient.KEY_CATEGORY)
    );

    public boolean isEnabled = false;
    private long day = -1;
    private String formattedDay = null;

    @Override
    public void onStartTick(Minecraft client) {
        while (DAY_VIEWER_KEY.consumeClick()) {
            isEnabled = !isEnabled;
            if (isEnabled) GhostifyClient.moduleList.showModule(this);
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
        long newDay = client.level.getDayTime() / 24000L;
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
        return isEnabled;
    }
}
