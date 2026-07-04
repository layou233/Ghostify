package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.mixin.AccessKeyMapping;
import com.launium.ghostify.client.util.SkyblockLocation;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.BitRandomSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class AutoClicker extends AbstractModule implements ClientTickEvents.StartTick {
    public static final AutoClicker INSTANCE = new AutoClicker();
    public static int MAX_CLICK_DELAY = 135;
    public static int MIN_CLICK_DELAY = 120;
    public static FloatFloatImmutablePair CPS = updateCPS();
    private static int MAX_MISS_DELAY = (int) (MAX_CLICK_DELAY * 0.6F);
    private static int MIN_MISS_DELAY = (int) (MIN_CLICK_DELAY * 0.6F);
    private static final int MIN_HOLD_TIME = 55;
    private static final int MAX_HOLD_TIME = 95;

    public static void resetCPS() {
        MAX_CLICK_DELAY = 135;
        MIN_CLICK_DELAY = 120;
        MAX_MISS_DELAY = (int) (MAX_CLICK_DELAY * 0.6F);
        MIN_MISS_DELAY = (int) (MIN_CLICK_DELAY * 0.6F);
        CPS = updateCPS();
    }

    public static void setCPS(float min, float max) {
        MAX_CLICK_DELAY = (int) (1000F / min);
        MIN_CLICK_DELAY = (int) (1000F / max);
        MAX_MISS_DELAY = (int) (MAX_CLICK_DELAY * 0.6F);
        MIN_MISS_DELAY = (int) (MIN_CLICK_DELAY * 0.6F);
        CPS = updateCPS();
    }

    private static FloatFloatImmutablePair updateCPS() {
        return FloatFloatImmutablePair.of(1000F / MAX_CLICK_DELAY, 1000F / MIN_CLICK_DELAY);
    }

    //private static final AutoClickerContainer autoClickerContainer = new AutoClickerContainer();
    private static final KeyMapping SWITCH_AUTO_CLICKER_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.ghostify.switch_auto_clicker", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, GhostifyClient.KEY_CATEGORY)
    );

    public boolean isEnabled = false;
    private final BitRandomSource random = (BitRandomSource) RandomSource.create();
    private long nextPressTime = 0;
    private long releaseTime = 0;
    private boolean simulating = false;
    private boolean simulatedDown = false;

    private int rollHoldDuration() {
        return random.nextIntBetweenInclusive(MIN_HOLD_TIME, MAX_HOLD_TIME);
    }

    private long rollClickInterval() {
        long interval = random.nextIntBetweenInclusive(MIN_CLICK_DELAY, MAX_CLICK_DELAY);
        if (random.next(3) == 0) { // simulate click miss, 1 in 2**3
            interval += random.nextIntBetweenInclusive(MIN_MISS_DELAY, MAX_MISS_DELAY);
        }
        return interval;
    }

    public boolean modulateAttackDown(boolean down) {
        return down && (!this.simulating || this.simulatedDown);
    }

    @Override
    public void onStartTick(@NonNull Minecraft client) {
        while (SWITCH_AUTO_CLICKER_KEY.consumeClick()) {
            this.isEnabled = !this.isEnabled;
        }
        if (this.isActive()) {
            //GhostifyClient.island.show(autoClickerContainer);
            GhostifyClient.moduleList.showModule(this);
        }
        AccessKeyMapping attack = (AccessKeyMapping) client.options.keyAttack;
        boolean shouldSimulate = this.isActive() &&
                client.player != null &&
                client.screen == null &&
                client.mouseHandler.isMouseGrabbed() &&
                attack.isRawDown();
        if (!shouldSimulate) {
            this.simulating = false;
            this.simulatedDown = false;
            return;
        }
        long now = Util.getMillis();
        if (!this.simulating) {
            // the first press is the player's own real click (GLFW already counted it);
            // take over from its release onwards
            this.simulating = true;
            this.releaseTime = now + rollHoldDuration();
            this.nextPressTime = now + rollClickInterval();
        } else {
            while (now >= this.nextPressTime) { // catch up on lag like queued GLFW events
                KeyMapping.click(attack.getKey());
                this.releaseTime = this.nextPressTime + rollHoldDuration();
                this.nextPressTime += rollClickInterval();
            }
        }
        this.simulatedDown = now < this.releaseTime;
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
                            context.getSource().sendFeedback(
                                    Component.literal("[Ghostify] Current Auto Clicker CPS range is ["
                                            + CPS.leftFloat() + ", " + CPS.rightFloat() + "]."));
                            return 0;
                        })
        );
    }

    @Override
    public String title() {
        return "AutoClicker";
    }

    @Override
    public @Nullable String subtitle() {
        return String.format("%.1f %.1f", CPS.leftFloat(), CPS.rightFloat());
    }

    @Override
    public boolean isActive() {
        return isEnabled &&
                !GhostPickaxe.INSTANCE.isActive() &&
                !(BetterDungeonbreaker.isHolding && SkyblockLocation.isInDungeons());
    }
}
