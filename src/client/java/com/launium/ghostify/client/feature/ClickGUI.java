package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.clickgui.ClickGUIScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

public class ClickGUI implements ClientTickEvents.StartTick {
    public static final ClickGUI INSTANCE = new ClickGUI();

    private static final KeyMapping CLICK_GUI_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.ghostify.show_click_gui", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), GhostifyClient.KEY_CATEGORY)
    );

    @Override
    public void onStartTick(@NonNull Minecraft client) {
        boolean clicked = false;
        while (CLICK_GUI_KEY.consumeClick()) {
            clicked = true;
        }
        if (clicked) {
            client.setScreenAndShow(new ClickGUIScreen(client, client.gui.screen()));
        }
    }
}
