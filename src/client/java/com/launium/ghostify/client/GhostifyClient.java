package com.launium.ghostify.client;

import com.launium.ghostify.client.feature.*;
import com.launium.ghostify.client.feature.experimentation.AbstractExperimentSolver;
import com.launium.ghostify.client.ui.ServerTPSContainer;
import com.launium.ghostify.client.ui.island.HudDynamicIsland;
import com.launium.ghostify.client.util.ClientTaskScheduler;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GhostifyClient implements ClientModInitializer {
    public static Logger LOGGER = LogUtils.getLogger();

    public static final HudDynamicIsland island = new HudDynamicIsland();

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(island);
        ClientTickEvents.START_CLIENT_TICK.register(GhostPickaxe.instance);
        ClientTickEvents.START_CLIENT_TICK.register(ServerTPSContainer.instance);
        ClientTickEvents.START_CLIENT_TICK.register(ClientTaskScheduler::whenClientStartTick);
        ClientReceiveMessageEvents.GAME.register(LifeSaverTimer.instance);
        ClientReceiveMessageEvents.GAME.register(new SpiritPetWarning());
        ClientReceiveMessageEvents.GAME.register(new AutoPetNotification());
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
                                LifeSaverTimer.instance.reset();
                                return 0;
                            }))
                    .then(literal("tps")
                            .executes(context -> {
                                ServerTPSContainer.instance.whenRespawn();
                                return 0;
                            }));
            var command = dispatcher.register(builder);
            dispatcher.register(ClientCommandManager.literal("gy").redirect(command));
        }));
    }
}
