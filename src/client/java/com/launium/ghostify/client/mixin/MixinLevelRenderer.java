package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.ui.RoundRectRenderer;
import com.launium.ghostify.client.ui.font.RenderedTextCache;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "endFrame", at = @At("HEAD"))
    private void ghostify$endFrame(CallbackInfo ci) {
        RoundRectRenderer.Uniform.clear();
        RenderedTextCache.whenRenderEnd();
    }
}
