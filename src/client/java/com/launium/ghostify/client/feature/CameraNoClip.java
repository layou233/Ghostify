package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class CameraNoClip extends AbstractModule implements ClientTickEvents.StartTick {
    public static final CameraNoClip INSTANCE = new CameraNoClip();

    private static final KeyMapping CAMERA_NO_CLIP_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.switch_camera_no_clip", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7, GhostifyClient.KEY_CATEGORY)
    );
    public boolean isEnabled = false;

    @Override
    public void onStartTick(Minecraft client) {
        while (CAMERA_NO_CLIP_KEY.consumeClick()) {
            isEnabled = !isEnabled;
            if (isEnabled) GhostifyClient.moduleList.showModule(this);
        }
    }

    @Override
    public String title() {
        return "CameraNoClip";
    }

    @Override
    public @Nullable String subtitle() {
        return null;
    }

    @Override
    public boolean isActive() {
        return isEnabled;
    }
}
