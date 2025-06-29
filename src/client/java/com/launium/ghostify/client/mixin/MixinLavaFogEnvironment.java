package com.launium.ghostify.client.mixin;

import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LavaFogEnvironment.class)
public class MixinLavaFogEnvironment {
    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSpectator()Z"))
    private static boolean ghostify$alwaysUsesSpectatorFluidFog(Entity instance) {
        return ConfigManager.PATCHES.USE_SPECTATOR_FOG || instance.isSpectator();
    }
}
