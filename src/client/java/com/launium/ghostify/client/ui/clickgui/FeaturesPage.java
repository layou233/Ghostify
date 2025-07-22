package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.feature.DayViewer;
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
                })
        ), 2F, LAYER_DEPTH + 1));
    }
}
