package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.util.SkyblockItem;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class ToggleUse extends AbstractModule implements ClientTickEvents.StartTick, BooleanSupplier {
    public static final ToggleUse INSTANCE = new ToggleUse();
    private static final ObjectOpenHashSet<String> FORCE_ITEM_IDS = new ObjectOpenHashSet<>(new String[]{
            "TRIBAL_SPEAR",
            // Hydro Can
            "HYDRO_CAN_1000", "HYDRO_CAN_TURBO_2000", "HYDRO_CAN_ULTRA_3000", "AQUAMASTER_HYDROMAX",
            // Rift
            "FROZEN_WATER_PUNGI", "TIME_GUN"
    });

    private boolean forceUse = false;
    private boolean lastState = false; // last state of forceUse
    private boolean isDown = false; // for HUD purpose only

    @Override
    public boolean getAsBoolean() {
        return forceUse;
    }

    @Override
    public void onStartTick(Minecraft client) {
        forceUse = false;

        if (client.player == null) return;
        if (ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE &&
                SkyblockItem.from(client.player.getMainHandItem())
                        .flatMap(SkyblockItem::getID)
                        .map(FORCE_ITEM_IDS::contains)
                        .orElse(false)
        ) {
            forceUse = true;
            lastState = true;
            if (isDown != client.options.keyUse.isDown()) {
                isDown = !isDown;
                moduleList.needResort = true;
            }
            GhostifyClient.moduleList.showModule(this);
        } else if (lastState) {
            lastState = false;
            client.options.keyUse.setDown(false);
        }
    }

    @Override
    public String title() {
        return "ToggleUse";
    }

    @Override
    public @Nullable String subtitle() {
        return isDown ? "ON" : "OFF";
    }

    @Override
    public boolean isActive() {
        return forceUse && ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE;
    }
}
