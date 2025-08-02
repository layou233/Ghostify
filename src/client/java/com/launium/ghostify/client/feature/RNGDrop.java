package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.ui.container.RNGDropContainer;
import com.launium.ghostify.client.util.MusicInstance;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

public class RNGDrop implements SimpleChatEventHandler.NonOverlay {
    public static final RNGDrop INSTANCE = new RNGDrop();

    private static final String RNG_MESSAGE_HEAD = "§d§lRNG METER! §r§aReselected the ";
    private static final String RNG_ITEM_SOURCE_SPLITTER = " §afor ";

    public long lastDropTimestamp = 0;
    public String itemName = "";
    public String sourceName;

    @Override
    public void onReceiveChat(String message) {
        if (ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY && message.startsWith(RNG_MESSAGE_HEAD)) {
            int endIndex = message.indexOf('!', RNG_MESSAGE_HEAD.length());
            if (endIndex < 0) endIndex = message.length();

            // itemAndSource example: "Second Master Star for Catacombs - Floor 4 (Master Mode)"
            String itemAndSource = message.substring(RNG_MESSAGE_HEAD.length(), endIndex);

            int splitIndex = itemAndSource.lastIndexOf(RNG_ITEM_SOURCE_SPLITTER);
            if (splitIndex < 0) {
                itemName = itemAndSource;
                sourceName = null;
            } else {
                itemName = itemAndSource.substring(0, splitIndex);
                sourceName = itemAndSource.substring(splitIndex + RNG_ITEM_SOURCE_SPLITTER.length());
            }

            lastDropTimestamp = Util.getMillis();
            RNGDropContainer.INSTANCE.resetAnimation();
            GhostifyClient.island.show(RNGDropContainer.INSTANCE);
            Minecraft.getInstance().getSoundManager().play(new MusicInstance("music_of_rng_drop",
                    "config/Ghostify/rng_music.ogg", false, lastDropTimestamp + 9000L));
        }
    }
}
