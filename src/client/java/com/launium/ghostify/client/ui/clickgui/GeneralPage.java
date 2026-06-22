package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;

import java.util.List;

public class GeneralPage extends AbstractPage {
    public GeneralPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Background blur", "Blur for this ClickGUI.", ConfigManager.GENERAL.CLICK_GUI_BLUR, newValue -> {
                    ConfigManager.GENERAL.CLICK_GUI_BLUR = newValue;
                    ConfigManager.GENERAL.markAsChanged();
                }),
                new ModuleItemView(client, "Press \"Speed Dial - Right\" key in game to open the speed dial menu.", "Open Abiphone once to sync your contact data. Click here for more info (W.I.P).",
                        () -> ConfirmLinkScreen.confirmLinkNow(client.gui.screen(), "https://launium.com/doc/Ghostify/SpeedDial", true)),
                new ModuleItemView(client, "More to come!", "This is a technical preview. Keep tuned to our updates!",
                        () -> {}),
                new ModuleItemView(client, "Click to buy us a cup of tea (Afdian)", "Cute cutie, give me money. >-<",
                        () -> ConfirmLinkScreen.confirmLinkNow(client.gui.screen(), "https://afdian.com/a/launium", true))
        ), 4F, LAYER_DEPTH + 1));
    }
}
