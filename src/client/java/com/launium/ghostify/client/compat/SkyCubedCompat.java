package com.launium.ghostify.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public class SkyCubedCompat {
    public static boolean IS_EXISTS = false;

    static void init(FabricLoader loader) {
        if (loader.isModLoaded("skycubed")) {
            IS_EXISTS = true;
        }
    }
}
