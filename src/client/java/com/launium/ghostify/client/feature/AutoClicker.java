package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.mixin.AccessKeyMapping;
import com.launium.ghostify.client.ui.container.AutoClickerContainer;
import com.launium.ghostify.client.util.Remember;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.BitRandomSource;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AutoClicker implements ClientTickEvents.StartTick {
    public static final AutoClicker INSTANCE = new AutoClicker();
    public static int MAX_CLICK_DELAY = 135;
    public static int MIN_CLICK_DELAY = 120;
    private static int MAX_MISS_DELAY = (int) (MAX_CLICK_DELAY * 0.6F);
    private static int MIN_MISS_DELAY = (int) (MIN_CLICK_DELAY * 0.6F);

    public static void resetCPS() {
        MAX_CLICK_DELAY = 135;
        MIN_CLICK_DELAY = 120;
        MAX_MISS_DELAY = (int) (MAX_CLICK_DELAY * 0.6F);
        MIN_MISS_DELAY = (int) (MIN_CLICK_DELAY * 0.6F);
    }

    public static void setCPS(float min, float max) {
        MAX_CLICK_DELAY = (int) (1000F / min);
        MIN_CLICK_DELAY = (int) (1000F / max);
        MAX_MISS_DELAY = (int) (MAX_CLICK_DELAY * 0.6F);
        MIN_MISS_DELAY = (int) (MIN_CLICK_DELAY * 0.6F);
    }

    public static FloatFloatImmutablePair getCPS() {
        return FloatFloatImmutablePair.of(1000F / MAX_CLICK_DELAY, 1000F / MIN_CLICK_DELAY);
    }

    private static final AutoClickerContainer autoClickerContainer = new AutoClickerContainer();
    private static final KeyMapping SWITCH_AUTO_CLICKER_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.switch_auto_clicker", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, KeyMapping.CATEGORY_GAMEPLAY)
    );

    public boolean isEnabled = false;
    private final BitRandomSource random = (BitRandomSource) RandomSource.create();
    private long breakingFinishTime = 0;
    private long nextLeftClickTime = 0;
    private Remember<Boolean> rememberLeftClick = new Remember<>(false);

    private long rollNextClickTime() {
        long next = Util.getMillis() + random.nextIntBetweenInclusive(MIN_CLICK_DELAY, MAX_CLICK_DELAY);
        if (random.next(3) == 0) { // simulate click miss, 1 in 2**3
            next += random.nextIntBetweenInclusive(MIN_MISS_DELAY, MAX_MISS_DELAY);
        }
        return next;
    }

    @Override
    public void onStartTick(Minecraft client) {
        if (SWITCH_AUTO_CLICKER_KEY.consumeClick()) {
            this.isEnabled = !this.isEnabled;
            if (this.isEnabled) GhostifyClient.island.show(autoClickerContainer);
        }
        if (this.isEnabled && client.player != null && client.screen == null) {
            boolean isLeftDown = client.options.keyAttack.isDown();
            boolean isUnchanged = rememberLeftClick.update(isLeftDown);
            boolean isBreaking = client.gameMode.isDestroying();
            long now = Util.getMillis();
            if (isUnchanged) {
                if (isLeftDown && now > nextLeftClickTime) {
                    if (!isBreaking && now > breakingFinishTime) {
                        KeyMapping.click(((AccessKeyMapping) client.options.keyAttack).getKey());
                    }
                    nextLeftClickTime = rollNextClickTime();
                }
            } else {
                breakingFinishTime = 0L;
                nextLeftClickTime = rollNextClickTime();
            }
            // break cooldown to avoid to trigger anti-cheat (FastBreak)
            if (isBreaking && client.gameMode.getDestroyStage() > 8) breakingFinishTime = now + 400L;
        }
    }

    public static void registerCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        var commandSet = argument("min", FloatArgumentType.floatArg(0))
                .then(argument("max", FloatArgumentType.floatArg(0))
                        .executes(context -> {
                            float min = FloatArgumentType.getFloat(context, "min");
                            float max = FloatArgumentType.getFloat(context, "max");
                            setCPS(min, max);
                            context.getSource().sendFeedback(
                                    Component.literal("[Ghostify] Updated Auto Clicker CPS range to ["
                                            + min + ", " + max + "]."));
                            return 0;
                        }));
        builder.then(
                literal("ac")
                        .then(commandSet)
                        .then(literal("set").then(commandSet))
                        .then(literal("reset").executes(context -> {
                            resetCPS();
                            context.getSource().sendFeedback(
                                    Component.literal("[Ghostify] Reset Auto Clicker CPS range to default done."));
                            return 0;
                        }))
                        .executes(context -> {
                            FloatFloatImmutablePair cps = AutoClicker.getCPS();
                            context.getSource().sendFeedback(
                                    Component.literal("[Ghostify] Current Auto Clicker CPS range is ["
                                            + cps.leftFloat() + ", " + cps.rightFloat() + "]."));
                            return 0;
                        })
        );
    }
}
