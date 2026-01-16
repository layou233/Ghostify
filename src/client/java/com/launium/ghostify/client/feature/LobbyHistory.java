package com.launium.ghostify.client.feature;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.ui.container.LobbyHistoryContainer;
import com.launium.ghostify.client.util.SimpleDuration;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.util.Util;

public class LobbyHistory implements SimpleChatEventHandler.NonOverlay, ClientPlayConnectionEvents.Init {
    public static final LobbyHistory INSTANCE = new LobbyHistory();

    private final Cache<Integer, Long> LOBBY_HISTORY_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .initialCapacity(16)
            .maximumSize(64)
            .build();
    private int lastLobbyHash = 0; // 0 equals to "".hashCode()

    @Override
    public void onReceiveChat(String text) {
        if (text.startsWith("Sending to server ") && text.endsWith("...")) {
            // process lobby code to compact format
            String lobbyCode = text.substring("Sending to server ".length(), text.indexOf('.'));
            if (lobbyCode.startsWith("mini")) lobbyCode = 'm' + lobbyCode.substring(4);
            else if (lobbyCode.startsWith("mega")) lobbyCode = 'M' + lobbyCode.substring(4);

            long now = Util.getMillis();
            int lobbyHash = lobbyCode.hashCode();

            // search for history
            Long historyTime = LOBBY_HISTORY_CACHE.getIfPresent(lobbyHash);
            if (historyTime != null) {
                LobbyHistoryContainer.INSTANCE.text = "Lobby " + lobbyCode + " ⚠ Last visit " +
                        new SimpleDuration(now - historyTime) + " ago";
                LobbyHistoryContainer.INSTANCE.lastTriggeredTimestamp = now;
                GhostifyClient.island.show(LobbyHistoryContainer.INSTANCE);
            }

            if (lastLobbyHash != 0) {
                LOBBY_HISTORY_CACHE.put(lastLobbyHash, now);
            }
            lastLobbyHash = lobbyHash;
        }
    }

    @Override
    public void onPlayInit(ClientPacketListener handler, Minecraft client) {
        lastLobbyHash = 0;
    }
}
