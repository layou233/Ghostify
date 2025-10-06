package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.ui.RoundRectRenderer;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
    @Inject(method = "flipFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;endFrame()V"))
    private static void endFrame(long l, TracyFrameCapture tracyFrameCapture, CallbackInfo ci) {
        RoundRectRenderer.Uniform.clear();
    }
}
