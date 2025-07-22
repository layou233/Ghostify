package com.launium.ghostify.client.compat;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;

public class SkyblockerCompat {
    public static boolean IS_EXISTS = false;

    static void init(FabricLoader loader) {
        if (loader.isModLoaded("skyblocker")) {
            IS_EXISTS = true;
        }
    }

    public static boolean isEffectOverlayHidden() {
        if (IS_EXISTS) {
            return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay;
        }
        return false;
    }
}
