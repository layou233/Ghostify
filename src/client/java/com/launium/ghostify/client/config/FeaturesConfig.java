package com.launium.ghostify.client.config;

import com.google.gson.annotations.SerializedName;

public class FeaturesConfig extends AbstractConfig {
    static String CONFIG_NAME = "features.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("enable_day_viewer")
    public boolean ENABLE_DAY_VIEWER = false;

    @SerializedName("enable_harp_bot")
    public boolean ENABLE_HARP_BOT = false;

    @SerializedName("enable_auto_tip")
    public boolean ENABLE_AUTO_TIP = false;

    @SerializedName("enable_rng_drop_summary")
    public boolean ENABLE_RNG_DROP_SUMMARY = true;

    @SerializedName("enable_foraging_style_warning")
    public boolean ENABLE_FORAGING_STYLE_WARNING = true;

    @SerializedName("enable_entrance_notifier")
    public boolean ENABLE_ENTRANCE_NOTIFIER = true;
}
