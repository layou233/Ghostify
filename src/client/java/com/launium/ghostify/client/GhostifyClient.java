package com.launium.ghostify.client;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.launium.ghostify.client.compat.Compat;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.feature.*;
import com.launium.ghostify.client.feature.experimentation.AbstractExperimentSolver;
import com.launium.ghostify.client.ui.RoundRectRenderer;
import com.launium.ghostify.client.ui.container.ServerTPSContainer;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.island.HudDynamicIsland;
import com.launium.ghostify.client.ui.modulelist.HudModuleList;
import com.launium.ghostify.client.ui.speeddial.HudSpeedDial;
import com.launium.ghostify.client.util.ClientTaskScheduler;
import com.launium.ghostify.client.util.SkyblockItem;
import com.launium.ghostify.client.util.SkyblockLocation;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GhostifyClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder()
            .setFormattingStyle(FormattingStyle.COMPACT.withNewline("\n"))
            .create();
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("ghostify", "main"));

    public static final ResourceLocation POST_PHASE = ResourceLocation.fromNamespaceAndPath("ghostify", "post");

    public static final HudDynamicIsland island = new HudDynamicIsland();
    public static final HudModuleList moduleList = new HudModuleList();
    public static final HudSpeedDial speedDial = new HudSpeedDial();

    @Override
    public void onInitializeClient() {
        // register phases
        ClientTickEvents.END_CLIENT_TICK.addPhaseOrdering(Event.DEFAULT_PHASE, POST_PHASE);

        // register events
        ConfigManager.init();
        Compat.init();
        FontManager.init();
        RoundRectRenderer.init();
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                ResourceLocation.fromNamespaceAndPath("ghostify", "dynamic_island"),
                island);
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                ResourceLocation.fromNamespaceAndPath("ghostify", "module_list"),
                moduleList);
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                ResourceLocation.fromNamespaceAndPath("ghostify", "speed_dial"),
                speedDial);
        ClientTickEvents.START_CLIENT_TICK.register(GhostPickaxe.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ServerTPSContainer.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ClientTaskScheduler::whenClientStartTick);
        ClientTickEvents.START_CLIENT_TICK.register(AutoClicker.INSTANCE);
        //ClientTickEvents.START_CLIENT_TICK.register(KuudraAutoPearl.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(CameraNoClip.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(HarpBot.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(DayViewer.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ClickGUI.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(SpeedDial.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(BetterDungeonbreaker.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ToggleUse.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(PickobulusPreview.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(POST_PHASE, SkyblockItem::clearCache);
        ClientReceiveMessageEvents.GAME.register(SimpleChatEventHandler.INSTANCE);
        ClientPlayConnectionEvents.INIT.register(LobbyHistory.INSTANCE);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.processChanges());
        WorldRenderEvents.END_EXTRACTION.register(PickobulusPreview.INSTANCE);
        WorldRenderEvents.AFTER_ENTITIES.register(PickobulusPreview.INSTANCE);
        ScreenEvents.BEFORE_INIT.register(SpeedDial.INSTANCE);
        UseBlockCallback.EVENT.register(DungeonPlaceFix.INSTANCE);
        AttackEntityCallback.EVENT.register(GoonBlocker.INSTANCE);
        AbstractExperimentSolver.init();
        HarpBot.INSTANCE.init();

        // register commands
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, buildContext) -> {
            var builder = ClientCommandManager.literal("ghostify")
                    .then(literal("harpDelayMultiplier")
                            .then(argument("min", FloatArgumentType.floatArg(0))
                                    .then(argument("max", FloatArgumentType.floatArg(0))
                                            .executes(context -> {
                                                HarpBot.delayMultiplierMin = FloatArgumentType.getFloat(context, "min");
                                                HarpBot.delayMultiplierMax = FloatArgumentType.getFloat(context, "max");
                                                context.getSource().sendFeedback(
                                                        Component.literal("[Ghostify] Updated harp delay multiplier to ["
                                                                + HarpBot.delayMultiplierMin + ", "
                                                                + HarpBot.delayMultiplierMax + "]."));
                                                return 0;
                                            }))))
                    .then(literal("resetLifeTimer")
                            .executes(context -> {
                                LifeSaverTimer.INSTANCE.reset();
                                return 0;
                            }))
                    .then(literal("resetAutoTip")
                            .executes(context -> {
                                AutoTip.INSTANCE.reset();
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
                            }))
                    .then(literal("whereAmI")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("[Ghostify] Location=\"" + SkyblockLocation.LOCATION_STRING +
                                        "\", isInDungeons=" + SkyblockLocation.isInDungeons()));
                                return 0;
                            }));
            AutoClicker.registerCommand(builder);
            moduleList.registerCommand(builder);
            var command = dispatcher.register(builder);
            dispatcher.register(ClientCommandManager.literal("gy").redirect(command));
        }));
    }
}
