package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.interfaces.AccessItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MixinPlayer {
    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void ghostify$cancelShortbowPullAnimation(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (((AccessItemStack) (Object) stack).ghostify$isShortbow()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
