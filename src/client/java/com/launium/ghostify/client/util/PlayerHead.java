package com.launium.ghostify.client.util;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PlayerHead {
    public static void setHeadSkin(ItemStack skull, String skinB64) {
        PropertyMap map = new PropertyMap();
        map.put("textures", new Property("textures", skinB64));
        ResolvableProfile profile = new ResolvableProfile(Optional.of("skull"), Optional.of(UUID.nameUUIDFromBytes(skinB64.getBytes())), map);
        skull.set(DataComponents.PROFILE, profile);
    }

    public static Optional<String> getSkinFromHead(@NotNull ItemStack skull) {
        ResolvableProfile profile = skull.get(DataComponents.PROFILE);
        if (profile == null) return Optional.empty();
        return profile.properties().get("textures").stream()
                .map(Property::value)
                .findAny();
    }
}
