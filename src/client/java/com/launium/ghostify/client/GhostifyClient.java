package com.launium.ghostify.client;

import com.launium.ghostify.client.feature.AutoPetNotification;
import com.launium.ghostify.client.feature.GhostPickaxe;
import com.launium.ghostify.client.feature.LifeSaverTimer;
import com.launium.ghostify.client.feature.SpiritPetWarning;
import com.launium.ghostify.client.ui.ServerTPSContainer;
import com.launium.ghostify.client.ui.island.HudDynamicIsland;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GhostifyClient implements ClientModInitializer {
    public static Logger LOGGER = LogUtils.getLogger();

    public static final HudDynamicIsland island = new HudDynamicIsland();

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(island);
        ClientTickEvents.START_CLIENT_TICK.register(GhostPickaxe.instance);
        ClientTickEvents.START_CLIENT_TICK.register(ServerTPSContainer.instance);
        ClientReceiveMessageEvents.GAME.register(LifeSaverTimer.instance);
        ClientReceiveMessageEvents.GAME.register(new SpiritPetWarning());
        ClientReceiveMessageEvents.GAME.register(new AutoPetNotification());
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, buildContext) -> {
            var builder = ClientCommandManager.literal("ghostify")
                    .then(literal("tps")
                            .executes(context -> {
                                ServerTPSContainer.instance.onRespawn();
                                return 0;
                            }))
                    .then(literal("resetLifeTimer")
                            .executes(context->{
                                LifeSaverTimer.instance.reset();
                                return 0;
                            }));
            var command = dispatcher.register(builder);
            dispatcher.register(ClientCommandManager.literal("gy").redirect(command));
        }));
    }
}
