package com.launium.ghostify.client;

import com.launium.ghostify.client.feature.*;
import com.launium.ghostify.client.feature.experimentation.AbstractExperimentSolver;
import com.launium.ghostify.client.ui.container.ServerTPSContainer;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.island.HudDynamicIsland;
import com.launium.ghostify.client.ui.modulelist.HudModuleList;
import com.launium.ghostify.client.util.ClientTaskScheduler;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GhostifyClient implements ClientModInitializer {
    public static Logger LOGGER = LogUtils.getLogger();
    public static final String KEY_CATEGORY = "category.ghostify.main";

    public static final HudDynamicIsland island = new HudDynamicIsland();
    public static final HudModuleList moduleList = new HudModuleList();

    @Override
    public void onInitializeClient() {
        FontManager.init();
        HudRenderCallback.EVENT.register(island);
        HudRenderCallback.EVENT.register(moduleList);
        ClientTickEvents.START_CLIENT_TICK.register(GhostPickaxe.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ServerTPSContainer.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ClientTaskScheduler::whenClientStartTick);
        ClientTickEvents.START_CLIENT_TICK.register(AutoClicker.INSTANCE);
        //ClientTickEvents.START_CLIENT_TICK.register(KuudraAutoPearl.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(CameraNoClip.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(HarpBot.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(PickobulusPreview.INSTANCE);
        ClientReceiveMessageEvents.GAME.register(LifeSaverTimer.INSTANCE);
        ClientReceiveMessageEvents.GAME.register(new SpiritPetWarning());
        ClientReceiveMessageEvents.GAME.register(new AutoPetNotification());
        ClientReceiveMessageEvents.GAME.register(PickobulusPreview.INSTANCE);
        ClientReceiveMessageEvents.GAME.register(LobbyHistory.INSTANCE);
        WorldRenderEvents.AFTER_ENTITIES.register(PickobulusPreview.INSTANCE);
        AbstractExperimentSolver.init();
        HarpBot.INSTANCE.init();
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
                    .then(literal("tps")
                            .executes(context -> {
                                ServerTPSContainer.INSTANCE.whenRespawn();
                                return 0;
                            }));
            AutoClicker.registerCommand(builder);
            moduleList.registerCommand(builder);
            var command = dispatcher.register(builder);
            dispatcher.register(ClientCommandManager.literal("gy").redirect(command));
        }));
    }
}
