package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.ui.RoundRectRenderState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiRenderer {
    @Shadow
    protected abstract boolean scissorChanged(@Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle oldScissorArea);

    @WrapOperation(method = "executeDraw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V"))
    private void ghostify$injectUniforms(RenderPass instance, int i, int j, int k, int l, Operation<Void> original, @Local(argsOnly = true) GuiRenderer.Draw draw) {
        RoundRectRenderState restoredRoundRectState = RoundRectRenderState.RESTORE.remove(System.identityHashCode(draw.textureSetup()));
        if (restoredRoundRectState != null && restoredRoundRectState.uniformBuffer != null) {
            instance.setUniform("u", restoredRoundRectState.uniformBuffer);
        }

        original.call(instance, i, j, k, l);
    }

    @Redirect(method = "addElementToMesh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;scissorChanged(Lnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)Z"))
    private boolean ghostify$forceNewBuffer(GuiRenderer instance, ScreenRectangle scissorArea, ScreenRectangle oldScissorArea, @Local(argsOnly = true) GuiElementRenderState renderState) {
        if (renderState instanceof RoundRectRenderState) {
            return true;
        }
        return scissorChanged(scissorArea, oldScissorArea);
    }

    @Inject(method = "draw", at = @At("RETURN"))
    private void ghostify$clearUniformBuffers(GpuBufferSlice bufferSlice, CallbackInfo ci) {
        RoundRectRenderState.RESTORE.clear();
        RoundRectRenderState.Uniforms.clear();
    }
}
