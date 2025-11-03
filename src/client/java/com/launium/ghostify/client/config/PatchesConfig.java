package com.launium.ghostify.client.config;

import com.google.gson.annotations.SerializedName;

public class PatchesConfig extends AbstractConfig {
    static String CONFIG_NAME = "patches.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("0_ping_dungeonbreaker")
    public boolean ZERO_PING_DUNGEONBREAKER = false;

    @SerializedName("use_spectator_fog")
    public boolean USE_SPECTATOR_FOG = true;

    @SerializedName("remove_suffocation_screen")
    public boolean REMOVE_SUFFOCATION_SCREEN = true;

    @SerializedName("cancel_shortbow_pull")
    public boolean CANCEL_SHORTBOW_PULL = true;

    @SerializedName("fix_dungeon_block_place")
    public boolean FIX_DUNGEON_BLOCK_PLACE = false;

    @SerializedName("no_command_execution_confirmation")
    public boolean NO_COMMAND_EXECUTION_CONFIRMATION = false;

    @SerializedName("overrule_skyblocker_glow_depth_test")
    public boolean OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST = true;
}
