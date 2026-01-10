package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.ui.container.RagnarockTimerContainer;
import com.launium.ghostify.client.util.SkyblockItem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RagnarockTimer implements SimpleChatEventHandler.Overlay {
    public static final RagnarockTimer INSTANCE = new RagnarockTimer();
    private static final String ID_RAGNAROCK = "RAGNAROCK_AXE";

    @Override
    public void onReceiveOverlay(String message) {
        if (message.endsWith("CASTING")) {
            // they may spam it a lot of times so we should guarantee we handle it only once
            if (RagnarockTimerContainer.INSTANCE.isActive()) return;

            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            ItemStack mainHandItem = player.getMainHandItem();
            SkyblockItem skyblockItem = SkyblockItem.from(mainHandItem).orElse(null);
            if (skyblockItem == null) return;
            if (mainHandItem.is(Items.GOLDEN_SWORD) && skyblockItem.getID().map(ID_RAGNAROCK::equals).orElse(false)) {
                Component strengthComponent = skyblockItem.getStyledLoreLines()
                        .flatMap(it -> it.stream()
                                .filter(line -> line.getString().startsWith("Strength: "))
                                .findAny())
                        .orElse(null);
                if (strengthComponent != null) {
                    String strengthText = strengthComponent.getString();
                    int endIndex = strengthText.indexOf(' ', "Strength: ".length());
                    strengthText = strengthText.substring("Strength: ".length(), endIndex > 0 ? endIndex : strengthText.length());

                    RagnarockTimerContainer.INSTANCE.lastTriggeredTimestamp = Util.getMillis();
                    RagnarockTimerContainer.INSTANCE.gainedStrength = Float.parseFloat(strengthText) * 1.5F;
                    GhostifyClient.island.show(RagnarockTimerContainer.INSTANCE);
                }
            }
        }
    }
}
