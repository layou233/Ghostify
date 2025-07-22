package com.launium.ghostify.client;

import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.launium.ghostify.client.ui.clickgui.ClickGUIScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;

public class GhostifyModMenuIntegration implements ModMenuApi {
    @SkipObfuscation
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new ClickGUIScreen(Minecraft.getInstance(), screen);
    }
}
