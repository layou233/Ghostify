package com.launium.ghostify.client.util;

import com.launium.ghostify.client.compat.SkyblockerCompat;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SkyblockLocation {
    public static @NonNull String LOCATION_STRING = "";

    @Getter
    private static boolean isInHypixel = false;

    private static boolean isInDungeons = false;

    public static void whenRespawn() {
        LOCATION_STRING = "";
        isInDungeons = false;
    }

    public static void whenServerBrandUpdate(String brand) {
        isInHypixel = brand.startsWith("Hypixel ");
    }

    public static void whenScoreboardUpdate(List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
        boolean foundLocationEntry = false;
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : entries) {
            Component name = entry.displayName();
            if (name == null) continue;
            String plainName = ChatFormatting.stripFormatting(name.getString()).trim();

            if (plainName.startsWith("Area: ")) {
                foundLocationEntry = true;
                isInDungeons = false;
            } else if (plainName.startsWith("Dungeon: ")) {
                foundLocationEntry = true;
                isInDungeons = true;
            }

            if (foundLocationEntry) {
                LOCATION_STRING = plainName.split(":", 2)[1].trim();
                break;
            }
        }
    }

    public static boolean isInDungeons() {
        if (SkyblockerCompat.IS_EXISTS) {
            return SkyblockerCompat.isInDungeons();
        }
        return isInDungeons;
    }
}
