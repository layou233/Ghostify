package com.launium.ghostify.client.feature.kuudra;

import com.google.common.collect.ImmutableList;
import com.launium.ghostify.client.mixin.AccessKeyMapping;
import com.launium.ghostify.client.util.ClientTaskScheduler;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class KuudraAutoPearl implements ClientTickEvents.StartTick {
    public static final KuudraAutoPearl INSTANCE = new KuudraAutoPearl();

    private static final ImmutableList<ObjectObjectImmutablePair<Item, String>> HOLDABLE_LIST = ImmutableList.of(
            ObjectObjectImmutablePair.of(Items.CHEST, "Elle's Supplies"),
            ObjectObjectImmutablePair.of(Items.PLAYER_HEAD, "Ballista Fuel Cell")
    );
    private final RandomSource random = RandomSource.create();
    private long lastScheduled = 0L;

    @Override
    public void onStartTick(Minecraft client) {
        if (client.player == null) return;
        long now = Util.getMillis();
        if (now - lastScheduled < 500L) return;
        Inventory inventory = client.player.getInventory();
        ItemStack lastHotbarItem = inventory.getItem(8);
        if (HOLDABLE_LIST.stream().anyMatch(holdable ->
                lastHotbarItem.getItem() == holdable.left() && holdable.right().equals(lastHotbarItem.getHoverName().getString())
        )) {
            // player is holding something
            ItemStack mainHandItem = inventory.getSelectedItem();
            if (mainHandItem.getItem() == Items.ENDER_PEARL && !mainHandItem.hasFoil()) {
                // not glinting; hopefully regular ender pearl
                // can also be Fel Pearl or something, but we assume the player got brain
                int selectedSlot = inventory.getSelectedSlot();
                //long delay = random.nextIntBetweenInclusive(0, 10);
                // wtf it's so fast; maybe Hypixel pre-sends slot switch packet?
                // use zero delay then, throw instantly or in next client tick
                ClientTaskScheduler.CLIENT_TASKS.add(new ClientTaskScheduler.AbstractTask(0) {
                    @Override
                    public void execute(Minecraft taskClient) {
                        if (taskClient.player == null) return;
                        Inventory taskInventory = taskClient.player.getInventory();
                        if (taskInventory.getSelectedSlot() == selectedSlot &&
                                taskInventory.getItem(selectedSlot).getItem() == Items.ENDER_PEARL) {
                            KeyMapping.click(((AccessKeyMapping) client.options.keyUse).getKey());
                        }
                    }
                });
                lastScheduled = now;
            }
        }
    }
}
