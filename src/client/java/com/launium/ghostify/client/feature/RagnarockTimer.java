package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.ui.container.RagnarockTimerContainer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class RagnarockTimer implements SimpleChatEventHandler.Overlay {
    public static final RagnarockTimer INSTANCE = new RagnarockTimer();

    @Override
    public void onReceiveOverlay(String message) {
        if (message.endsWith("CASTING")) {
            // they may spam it a lot of times so we should guarantee we handle it only once
            if (RagnarockTimerContainer.INSTANCE.isActive()) return;

            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.is(Items.GOLDEN_SWORD) && mainHandItem.getHoverName().getString().endsWith("Ragnarock")) {
                Component strengthComponent = mainHandItem.getComponents().getOrDefault(DataComponents.LORE, ItemLore.EMPTY)
                        .styledLines().stream().filter(line -> line.getString().startsWith("Strength: "))
                        .findAny().orElse(null);
                if (strengthComponent != null) {
                    String strengthText = strengthComponent.getString();
                    int endIndex = strengthText.indexOf(' ', "Strength: ".length());
                    strengthText = strengthText.substring("Strength: ".length(), endIndex > 0 ? endIndex : strengthText.length());

                    RagnarockTimerContainer.INSTANCE.lastTriggeredTimestamp = Util.getMillis();
                    RagnarockTimerContainer.INSTANCE.gainedStrength = Integer.parseInt(strengthText) * 1.5F;
                    GhostifyClient.island.show(RagnarockTimerContainer.INSTANCE);
                }
            }
        }
    }
}
