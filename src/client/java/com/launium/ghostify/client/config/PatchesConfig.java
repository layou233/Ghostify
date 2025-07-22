package com.launium.ghostify.client.config;

import com.google.gson.annotations.SerializedName;

public class PatchesConfig extends AbstractConfig {
    static String CONFIG_NAME = "patches.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("use_spectator_fog")
    public boolean USE_SPECTATOR_FOG = true;

    @SerializedName("remove_suffocation_screen")
    public boolean REMOVE_SUFFOCATION_SCREEN = true;

    @SerializedName("cancel_shortbow_pull")
    public boolean CANCEL_SHORTBOW_PULL = true;

    @SerializedName("overrule_skyblocker_glow_depth_test")
    public boolean OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST = true;
}
