package com.launium.ghostify.client.feature;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.ui.container.LobbyHistoryContainer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class LobbyHistory implements SimpleChatEventHandler.NonOverlay, ClientPlayConnectionEvents.Join {
    public static final LobbyHistory INSTANCE = new LobbyHistory();

    private final Cache<Integer, LocalTime> LOBBY_HISTORY_CACHE = CacheBuilder.newBuilder()
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

            LocalTime nowLocal = LocalTime.now();
            int lobbyHash = lobbyCode.hashCode();

            // search for history
            LocalTime historyTime = LOBBY_HISTORY_CACHE.getIfPresent(lobbyHash);
            if (historyTime != null) {
                LobbyHistoryContainer.INSTANCE.text = "Lobby " + lobbyCode + " ⚠ Last visit " +
                        Duration.between(historyTime, nowLocal).truncatedTo(ChronoUnit.SECONDS).toString().substring(2).toLowerCase() +
                        " ago";
                LobbyHistoryContainer.INSTANCE.lastTriggeredTimestamp = Util.getMillis();
                GhostifyClient.island.show(LobbyHistoryContainer.INSTANCE);
            }

            if (lastLobbyHash != 0) {
                LOBBY_HISTORY_CACHE.put(lastLobbyHash, nowLocal);
            }
            lastLobbyHash = lobbyHash;
        }
    }

    @Override
    public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        lastLobbyHash = 0;
    }
}
