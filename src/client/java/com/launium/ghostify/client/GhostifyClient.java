package com.launium.ghostify.client;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.launium.ghostify.client.compat.Compat;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.feature.*;
import com.launium.ghostify.client.feature.experimentation.AbstractExperimentSolver;
import com.launium.ghostify.client.ui.container.ServerTPSContainer;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.island.HudDynamicIsland;
import com.launium.ghostify.client.ui.modulelist.HudModuleList;
import com.launium.ghostify.client.util.ClientTaskScheduler;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GhostifyClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder()
            .setFormattingStyle(FormattingStyle.COMPACT.withNewline("\n"))
            .create();
    public static final String KEY_CATEGORY = "category.ghostify.main";

    public static final HudDynamicIsland island = new HudDynamicIsland();
    public static final HudModuleList moduleList = new HudModuleList();

    @Override
    public void onInitializeClient() {
        ConfigManager.init();
        Compat.init();
        FontManager.init();
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
            layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS,
                    ResourceLocation.fromNamespaceAndPath("ghostify", "dynamic_island"),
                    island);
            layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS,
                    ResourceLocation.fromNamespaceAndPath("ghostify", "module_list"),
                    moduleList);
        });
        ClientTickEvents.START_CLIENT_TICK.register(ServerTPSContainer.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ClientTaskScheduler::whenClientStartTick);
        //ClientTickEvents.START_CLIENT_TICK.register(KuudraAutoPearl.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(DayViewer.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ClickGUI.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(PickobulusPreview.INSTANCE);
        ClientReceiveMessageEvents.GAME.register(LifeSaverTimer.INSTANCE);
        ClientReceiveMessageEvents.GAME.register(new SpiritPetWarning());
        ClientReceiveMessageEvents.GAME.register(new AutoPetNotification());
        ClientReceiveMessageEvents.GAME.register(PickobulusPreview.INSTANCE);
        ClientReceiveMessageEvents.GAME.register(LobbyHistory.INSTANCE);
        ClientPlayConnectionEvents.JOIN.register(LobbyHistory.INSTANCE);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.processChanges());
        WorldRenderEvents.AFTER_ENTITIES.register(PickobulusPreview.INSTANCE);
        AbstractExperimentSolver.init();
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, buildContext) -> {
            var builder = ClientCommandManager.literal("ghostify")
                    .then(literal("resetLifeTimer")
                            .executes(context -> {
                                LifeSaverTimer.INSTANCE.reset();
                                return 0;
                            }))
                    .then(literal("tps")
                            .executes(context -> {
                                ServerTPSContainer.INSTANCE.whenRespawn();
                                return 0;
                            }))
                    .then(literal("configSave")
                            .executes(context -> {
                                ConfigManager.processChanges();
                                context.getSource().sendFeedback(Component.literal("[Ghostify] Processed config changes."));
                                return 0;
                            }));
            moduleList.registerCommand(builder);
            var command = dispatcher.register(builder);
            dispatcher.register(ClientCommandManager.literal("gy").redirect(command));
        }));
    }
}
