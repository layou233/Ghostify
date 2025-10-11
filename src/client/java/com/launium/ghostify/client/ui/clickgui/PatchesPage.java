package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;

import java.util.List;

public class PatchesPage extends AbstractPage {
    public PatchesPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "0 Ping Dungeonbreaker", "Ignore mining fatigue when holding Dungeonbreaker in Dungeons.", ConfigManager.PATCHES.ZERO_PING_DUNGEONBREAKER, newValue -> {
                    ConfigManager.PATCHES.ZERO_PING_DUNGEONBREAKER = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Always use spectator fog", "Clear in-lava/in-powder-snow camera.", ConfigManager.PATCHES.USE_SPECTATOR_FOG, newValue -> {
                    ConfigManager.PATCHES.USE_SPECTATOR_FOG = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Remove suffocation screen", "Remove the block texture that covers your whole screen when suffocating.", ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN, newValue -> {
                    ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Cancel shortbow pull animation", "Avoid pulling shortbow when you have arrows in your inventory.", ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL, newValue -> {
                    ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "No command execution confirmation", "Skip the confirmation dialog from Mojang. Just execute it.", ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION, newValue -> {
                    ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                })
        ), 4F, LAYER_DEPTH + 1));
    }
}
