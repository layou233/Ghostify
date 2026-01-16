package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.ui.container.AutoPetNotificationContainer;
import net.minecraft.util.Util;

public class AutoPetNotification implements SimpleChatEventHandler.NonOverlay {
    public static final AutoPetNotification INSTANCE = new AutoPetNotification();

    private static final String AUTO_PET_PREFIX = "§cAutopet §eequipped your ";

    @Override
    public void onReceiveChat(String text) {
        if (text.startsWith(AUTO_PET_PREFIX)) {
            text = text.substring(AUTO_PET_PREFIX.length(), text.indexOf('!', AUTO_PET_PREFIX.length()) - 2);
            AutoPetNotificationContainer.instance.warningText = "♣ Autopet equipped " + text;
            AutoPetNotificationContainer.instance.lastTriggeredTimestamp = Util.getMillis();
            GhostifyClient.island.show(AutoPetNotificationContainer.instance);
        }
    }
}
