package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;

import java.util.List;

public class PatchesPage extends AbstractPage {
    public PatchesPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Always use spectator fog", "Clear in-lava/in-powder-snow camera.", ConfigManager.PATCHES.USE_SPECTATOR_FOG, newValue -> {
                    ConfigManager.PATCHES.USE_SPECTATOR_FOG = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Remove suffocation screen", "Remove the block texture that covers your whole screen when suffocating.", ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN, newValue -> {
                    ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Cancel shortbow pull animation", "Avoid pulling shortbow when you have arrows in your inventory.", ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL, (newValue) -> {
                    ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Overrule Skyblocker glowing depth test", "Kinda buggy.", ConfigManager.PATCHES.OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST, (newValue) -> {
                    ConfigManager.PATCHES.OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                })
        ), 2F, LAYER_DEPTH + 1));
    }
}
