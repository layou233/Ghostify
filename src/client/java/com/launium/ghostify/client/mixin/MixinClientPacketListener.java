package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.ui.ServerTPSContainer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(method = "handleSetTime", at = @At("TAIL"))
    private void ghostify$handleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        ServerTPSContainer.instance.whenClientboundSetTime();
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void ghostify$handleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        ServerTPSContainer.instance.whenRespawn();
    }
}
