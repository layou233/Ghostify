package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.container.LifeSaverTimerContainer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.Arrays;

public class LifeSaverTimer implements ClientReceiveMessageEvents.Game {
    public static final LifeSaverTimer INSTANCE = new LifeSaverTimer();

    public enum LifeSavers {
        BONZO_MASK("Bonzo's Mask", 360_000, true), // based on catacomb level
        SPIRIT_MASK("Spirit Mask", 30_000, false),
        PHOENIX_PET("Phoenix Pet", 60_000, false);

        public String name;
        public long cooldown; // in milliseconds
        public boolean shouldReadLore;

        LifeSavers(String name, long cooldown, boolean shouldReadLore) {
            this.name = name;
            this.cooldown = cooldown;
            this.shouldReadLore = shouldReadLore;
        }
    }

    public LifeSavers lastTriggered;
    public int invincibleTicks = 0;
    public long[] availableTimestamp = new long[LifeSavers.values().length];

    public void reset() {
        invincibleTicks = 0;
        Arrays.fill(availableTimestamp, 0);
    }

    @Override
    public void onReceiveGameMessage(Component message, boolean isOverlay) {
        if (isOverlay) return;
        String plain = message.getString();
        LifeSavers triggered;
        if (plain.startsWith("Your ") && plain.endsWith("Bonzo's Mask saved your life!")) {
            triggered = LifeSavers.BONZO_MASK;
        } else if (plain.equals("Second Wind Activated! Your Spirit Mask saved your life!")) {
            triggered = LifeSavers.SPIRIT_MASK;
        } else if (plain.equals("Your Phoenix Pet saved you from certain death!")) {
            triggered = LifeSavers.PHOENIX_PET;
        } else {
            return;
        }

        long cooldownTime = triggered.cooldown;
        if (triggered.shouldReadLore) {
            try {
                cooldownTime = readCooldownFromHelmet();
            } catch (Exception e) {
                GhostifyClient.LOGGER.error("Failed to read cooldown from item lore, will use default", e);
            }
        }

        this.invincibleTicks = 60; // 3s
        this.availableTimestamp[triggered.ordinal()] = cooldownTime + Util.getMillis();
        this.lastTriggered = triggered;
        GhostifyClient.island.show(LifeSaverTimerContainer.instance);
    }

    public void whenServerTick() {
        if (invincibleTicks > 0) invincibleTicks--;
    }

    private long readCooldownFromHelmet() {
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        ItemStack helmet = inventory.getItem(EquipmentSlot.HEAD.getIndex(36));
        if (helmet.isEmpty()) { // how?
            throw new IllegalStateException("failed to read lore from helmet: helmet is empty");
        }
        Component cooldownComponent = helmet.getComponents().getOrDefault(DataComponents.LORE, ItemLore.EMPTY)
                .styledLines().stream().filter(line -> line.getString().startsWith("Cooldown: "))
                .findAny().orElse(null);
        if (cooldownComponent == null) { // why?
            throw new IllegalStateException("failed to read lore from helmet: cooldown is not found");
        }
        String secondsText = cooldownComponent.getString().trim();
        secondsText = secondsText.substring("Cooldown: ".length(), secondsText.length() - 1);
        return Long.parseLong(secondsText) * 1000;
    }
}
