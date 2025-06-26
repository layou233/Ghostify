package com.launium.ghostify.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public class Compat {
    public static void init() {
        FabricLoader loader = FabricLoader.getInstance();
        SkyCubedCompat.init(loader);
        SkyblockerCompat.init(loader);
    }
}
