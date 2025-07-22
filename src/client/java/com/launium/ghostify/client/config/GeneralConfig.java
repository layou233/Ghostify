package com.launium.ghostify.client.config;

import com.google.gson.annotations.SerializedName;

public class GeneralConfig extends AbstractConfig {
    static String CONFIG_NAME = "general.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("click_gui_blur")
    public boolean CLICK_GUI_BLUR = true;
}
