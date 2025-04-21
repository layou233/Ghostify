package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.SpiritPetWarningContainer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

public class SpiritPetWarning implements ClientReceiveMessageEvents.Game {
    @Override
    public void onReceiveGameMessage(Component message, boolean isOverlay) {
        if (isOverlay) return;
        String text = message.getString();
        if (text.startsWith("Your Spirit Pet hit ")) {
            SpiritPetWarningContainer.instance.lastTriggeredTimestamp = Util.getMillis();
            GhostifyClient.island.show(SpiritPetWarningContainer.instance);
        }
    }
}
