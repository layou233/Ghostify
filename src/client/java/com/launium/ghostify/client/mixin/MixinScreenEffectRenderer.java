package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.config.ConfigManager;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {
    @WrapWithCondition(
            method = "renderScreenEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderTex(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    )
    private static boolean ghostify$removeSuffocationScreen(TextureAtlasSprite texture, PoseStack poseStack, MultiBufferSource bufferSource) {
        return !ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN;
    }
}
