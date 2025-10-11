package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.feature.BetterDungeonbreaker;
import com.launium.ghostify.client.interfaces.AccessItemStack;
import com.launium.ghostify.client.util.SkyblockLocation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
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
        if (ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL && ((AccessItemStack) (Object) stack).ghostify$isShortbow()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @WrapOperation(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean ghostify$0PingDungeonbreaker(Player instance, Holder<MobEffect> effect, Operation<Boolean> original) {
        if (ConfigManager.PATCHES.ZERO_PING_DUNGEONBREAKER &&
                effect == MobEffects.MINING_FATIGUE &&
                BetterDungeonbreaker.isHolding &&
                SkyblockLocation.isInDungeons()) {
            return false;
        }
        return original.call(instance, effect);
    }
}
