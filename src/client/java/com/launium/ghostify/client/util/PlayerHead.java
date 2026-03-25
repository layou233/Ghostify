package com.launium.ghostify.client.util;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerHead {
    public static void setHeadSkin(ItemStack skull, String skinB64) {
        PropertyMap properties = ExtraCodecs.PROPERTY_MAP.parse(JsonOps.INSTANCE, JsonParser.parseString("[{\"name\":\"textures\",\"value\":\"" + skinB64 + "\"}]")).getOrThrow();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Ghostify Fake Profile", properties);
        skull.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
    }

    public static Optional<String> getSkinFromHead(@NonNull ItemStack skull) {
        ResolvableProfile profile = skull.get(DataComponents.PROFILE);
        if (profile == null) return Optional.empty();
        return profile.partialProfile().properties().get("textures").stream()
                .filter(Objects::nonNull)
                .map(Property::value)
                .findAny();
    }
}
