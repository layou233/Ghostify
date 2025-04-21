package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.AutoPetNotificationContainer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

public class AutoPetNotification implements ClientReceiveMessageEvents.Game {
    private static final String AUTO_PET_PREFIX = "§cAutopet §eequipped your ";

    @Override
    public void onReceiveGameMessage(Component message, boolean isOverlay) {
        if (isOverlay) return;
        String text = message.getString();
        if (text.startsWith(AUTO_PET_PREFIX)) {
            text = text.substring(AUTO_PET_PREFIX.length(), text.indexOf('!', AUTO_PET_PREFIX.length()) - 2);
            AutoPetNotificationContainer.instance.warningText = "♣ Autopet equipped " + text;
            AutoPetNotificationContainer.instance.lastTriggeredTimestamp = Util.getMillis();
            GhostifyClient.island.show(AutoPetNotificationContainer.instance);
        }
    }
}
