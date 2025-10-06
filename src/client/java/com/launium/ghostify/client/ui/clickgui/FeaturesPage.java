package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.feature.AutoTip;
import com.launium.ghostify.client.feature.DayViewer;
import com.launium.ghostify.client.feature.HarpBot;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;

import java.util.List;

public class FeaturesPage extends AbstractPage {
    public FeaturesPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Day viewer", "Display the date of the current world.", ConfigManager.FEATURES.ENABLE_DAY_VIEWER, newValue -> {
                    ConfigManager.FEATURES.ENABLE_DAY_VIEWER = newValue;
                    if (ConfigManager.FEATURES.ENABLE_DAY_VIEWER) GhostifyClient.moduleList.showModule(DayViewer.INSTANCE);
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Harp bot", "Rhythm games should have autoplay.", ConfigManager.FEATURES.ENABLE_HARP_BOT, newValue -> {
                    ConfigManager.FEATURES.ENABLE_HARP_BOT = newValue;
                    if (ConfigManager.FEATURES.ENABLE_HARP_BOT) GhostifyClient.moduleList.showModule(HarpBot.INSTANCE);
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Auto tip", "Send /tipall regularly when in Hypixel.", ConfigManager.FEATURES.ENABLE_AUTO_TIP, newValue -> {
                    ConfigManager.FEATURES.ENABLE_AUTO_TIP = newValue;
                    if (ConfigManager.FEATURES.ENABLE_AUTO_TIP) {
                        AutoTip.INSTANCE.setupTask();
                        GhostifyClient.moduleList.showModule(AutoTip.INSTANCE);
                    }
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "RNG drop summary", "Notify the RNG drop, and play the music at \"config/Ghostify/rng_music.ogg\". GG!", ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY, newValue -> {
                    ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Foraging style warning", "Warn when you chop trees in the wrong order to prevent the loss of foraging efficiency.", ConfigManager.FEATURES.ENABLE_FORAGING_STYLE_WARNING, newValue -> {
                    ConfigManager.FEATURES.ENABLE_FORAGING_STYLE_WARNING = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Entrance notifier", "Pop up a system notification when entering Dungeon/Kuudra if the game window is not focused.", ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER, newValue -> {
                    ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                })
        ), 2F, LAYER_DEPTH + 1));
    }
}
