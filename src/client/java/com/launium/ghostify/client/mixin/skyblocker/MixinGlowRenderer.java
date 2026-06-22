package com.launium.ghostify.client.mixin.skyblocker;

import com.launium.ghostify.client.config.ConfigManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlowRenderer.class)
@Pseudo
public class MixinGlowRenderer {
    @Inject(method = "updateGlowDepthTexDepth",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/hysky/skyblocker/utils/render/GlowRenderer;tryUpdateDepthTexture()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true)
    private void ghostify$preventSkyblockerGlowDepthTest(CallbackInfo ci) {
        if (ConfigManager.PATCHES.OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST) {
            RenderSystem.getDevice().createCommandEncoder()
                    .clearDepthTexture(GlowRenderer.INSTANCE.getGlowDepthTexture().texture(), 0.0);
            ci.cancel();
        }
    }
}
