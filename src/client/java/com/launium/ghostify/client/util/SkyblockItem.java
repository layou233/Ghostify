package com.launium.ghostify.client.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // intended for lazy init
public class SkyblockItem {
    private static final Object2ObjectOpenHashMap<ItemStack, SkyblockItem> CACHE = new Object2ObjectOpenHashMap<>();

    public ItemStack itemStack;

    private CompoundTag customData;
    private Optional<String> id;
    private Optional<List<Component>> styledLoreLines;

    private SkyblockItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static Optional<SkyblockItem> from(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return Optional.empty();
        SkyblockItem instance = CACHE.get(itemStack);
        if (instance != null) {
            return Optional.of(instance);
        }

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return Optional.empty();

        instance = new SkyblockItem(itemStack);
        instance.customData = customData.copyTag();
        CACHE.put(itemStack, instance);
        return Optional.of(instance);
    }

    public static void clearCache(Minecraft client) {
        CACHE.clear();
    }

    public Optional<String> getID() {
        if (id == null) {
            id = Optional.ofNullable(customData.get("id"))
                    .flatMap(Tag::asString);
        }
        return id;
    }

    public Optional<List<Component>> getStyledLoreLines() {
        if (styledLoreLines == null) {
            styledLoreLines = Optional.ofNullable(itemStack.get(DataComponents.LORE))
                    .map(ItemLore::styledLines);
        }
        return styledLoreLines;
    }
}
