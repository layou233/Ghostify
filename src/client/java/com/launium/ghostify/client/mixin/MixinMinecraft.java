package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.feature.AutoClicker;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean ghostify$simulateAttackHold(KeyMapping instance, Operation<Boolean> original) {
        boolean down = original.call(instance);
        if (instance == ((Minecraft) (Object) this).options.keyAttack) {
            return AutoClicker.INSTANCE.modulateAttackDown(down);
        }
        return down;
    }
}
