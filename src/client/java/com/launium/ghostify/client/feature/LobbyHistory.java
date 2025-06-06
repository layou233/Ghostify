package com.launium.ghostify.client.feature;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.container.LobbyHistoryContainer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

public class LobbyHistory implements ClientReceiveMessageEvents.Game {
    public static final LobbyHistory INSTANCE = new LobbyHistory();

    private final Cache<Integer, LocalTime> LOBBY_HISTORY_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .initialCapacity(16)
            .maximumSize(64)
            .build();

    @Override
    public void onReceiveGameMessage(Component message, boolean isOverlay) {
        if (isOverlay) return;
        String text = message.getString();
        if (text.startsWith("Sending to server ") && text.endsWith("...")) {
            String lobbyCode = text.substring("Sending to server ".length(), text.indexOf('.'));
            if (lobbyCode.startsWith("mini")) lobbyCode = 'm' + lobbyCode.substring(4);
            else if (lobbyCode.startsWith("mega")) lobbyCode = 'M' + lobbyCode.substring(4);
            LocalTime nowLocal = LocalTime.now();
            int lobbyHash = lobbyCode.hashCode();
            try {
                LocalTime historyTime = LOBBY_HISTORY_CACHE.get(lobbyHash, () -> nowLocal);
                if (!nowLocal.equals(historyTime)) {
                    LobbyHistoryContainer.INSTANCE.text = "Lobby " + lobbyCode + " ⚠ Last visit " +
                            Duration.between(historyTime, nowLocal).truncatedTo(ChronoUnit.SECONDS).toString().substring(2).toLowerCase() +
                            " ago";
                    LobbyHistoryContainer.INSTANCE.lastTriggeredTimestamp = Util.getMillis();
                    GhostifyClient.island.show(LobbyHistoryContainer.INSTANCE);
                    LOBBY_HISTORY_CACHE.put(lobbyHash, nowLocal);
                }
            } catch (ExecutionException ignored) {
            }
        }
    }
}
