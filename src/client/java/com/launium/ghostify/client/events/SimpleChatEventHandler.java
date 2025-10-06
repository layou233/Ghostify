package com.launium.ghostify.client.events;

import com.launium.ghostify.client.feature.*;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;

public class SimpleChatEventHandler implements ClientReceiveMessageEvents.Game {
    public static final SimpleChatEventHandler INSTANCE = new SimpleChatEventHandler();

    @Override
    public void onReceiveGameMessage(Component message, boolean isOverlay) {
        String text = message.getString();
        if (isOverlay) {
            // :(
        } else {
            LifeSaverTimer.INSTANCE.onReceiveChat(text);
            SpiritPetWarning.INSTANCE.onReceiveChat(text);
            AutoPetNotification.INSTANCE.onReceiveChat(text);
            PickobulusPreview.INSTANCE.onReceiveChat(text);
            LobbyHistory.INSTANCE.onReceiveChat(text);
            RNGDrop.INSTANCE.onReceiveChat(text);
            EntranceNotifier.INSTANCE.onReceiveChat(text);
        }
    }

    public interface NonOverlay {
        void onReceiveChat(String message);
    }

    public interface Overlay {
        void onReceiveOverlay(String message);
    }
}
