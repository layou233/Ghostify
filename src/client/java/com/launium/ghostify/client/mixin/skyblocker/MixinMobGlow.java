package com.launium.ghostify.client.mixin.skyblocker;

import com.launium.ghostify.client.config.ConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobGlow.class, remap = false)
@Pseudo
public class MixinMobGlow {
    @Inject(method = "getMobGlowOrDefault", at = @At("HEAD"), cancellable = true)
    private static void ghostify$alwaysUseVanillaGlow(Entity entity, int defaultColor, CallbackInfoReturnable<Integer> cir) {
        if (ConfigManager.PATCHES.OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST &&
                defaultColor == MobGlow.NO_GLOW) {
            cir.setReturnValue(MobGlow.NO_GLOW);
        }
    }
}
