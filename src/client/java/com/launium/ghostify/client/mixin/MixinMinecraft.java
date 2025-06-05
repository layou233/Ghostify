package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.feature.AutoClicker;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @WrapWithCondition(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
    public boolean ghostify$handleAutoClicker(Minecraft instance, boolean leftClick) {
        if (!leftClick || instance.player == null) return true;
        if (AutoClicker.INSTANCE.isEnabled) {
            // we are unable to handle mining fatigue by now
            // mining islands etc. are really buggy
            if (instance.player.hasEffect(MobEffects.MINING_FATIGUE)) {
                instance.gameMode.stopDestroyBlock();
                return false;
            }
            boolean isDestroying = instance.gameMode.getDestroyStage() > 0;
            if (isDestroying) instance.gameMode.stopDestroyBlock();
            return !isDestroying;
        }
        return true;
    }
}
