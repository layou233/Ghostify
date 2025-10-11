package com.launium.ghostify.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BetterDungeonbreaker implements ClientTickEvents.StartTick {
    public static final BetterDungeonbreaker INSTANCE = new BetterDungeonbreaker();
    private static final String NAME_DUNGEONBREAKER = "§cDungeonbreaker";

    public static boolean isHolding;
    public static int slot = -1;

    @Override
    public void onStartTick(Minecraft client) {
        if (client.player == null) {
            isHolding = false; // defensive
            return;
        }

        Inventory inventory = client.player.getInventory();
        slot = -1;
        for (int i = 0; i < 9; i++) { // hotbar slots
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.is(Items.DIAMOND_PICKAXE) && // fast check
                    NAME_DUNGEONBREAKER.equals(itemStack.getHoverName().getString())) {
                slot = i;
                break;
            }
        }

        isHolding = slot >= 0 && slot == inventory.getSelectedSlot();
    }
}
