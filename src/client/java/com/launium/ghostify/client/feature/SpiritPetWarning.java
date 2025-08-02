package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.ui.container.SpiritPetWarningContainer;
import net.minecraft.Util;

public class SpiritPetWarning implements SimpleChatEventHandler.NonOverlay {
    public static final SpiritPetWarning INSTANCE = new SpiritPetWarning();

    @Override
    public void onReceiveChat(String text) {
        if (text.startsWith("Your Spirit Pet hit ")) {
            SpiritPetWarningContainer.instance.lastTriggeredTimestamp = Util.getMillis();
            GhostifyClient.island.show(SpiritPetWarningContainer.instance);
        }
    }
}
