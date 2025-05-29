package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.feature.CameraNoClip;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {
    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void ghostify$cameraNoClip(float maxZoom, CallbackInfoReturnable<Float> cir) {
        if (CameraNoClip.INSTANCE.isEnabled) {
            cir.setReturnValue(maxZoom);
        }
    }
}
