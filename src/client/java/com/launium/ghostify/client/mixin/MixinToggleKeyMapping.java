package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.feature.ToggleUse;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.BooleanSupplier;

@Mixin(ToggleKeyMapping.class)
public class MixinToggleKeyMapping {
    @ModifyVariable(
            method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;Ljava/util/function/BooleanSupplier;Z)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static BooleanSupplier ghostify$wrapToggleUseSupplier(BooleanSupplier supplier, @Local(argsOnly = true) String name) {
        return switch (name) {
            case "key.use" -> () -> {
                if (ToggleUse.INSTANCE.getAsBoolean()) return true;
                return supplier.getAsBoolean();
            };
            default -> supplier;
        };
    }
}
