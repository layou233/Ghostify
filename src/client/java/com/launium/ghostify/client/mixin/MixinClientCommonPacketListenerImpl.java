package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.feature.AutoTip;
import com.launium.ghostify.client.feature.LifeSaverTimer;
import com.launium.ghostify.client.util.SkyblockLocation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class MixinClientCommonPacketListenerImpl {
    @Inject(method = "handlePing", at = @At("TAIL"))
    private void ghostify$handlePing(ClientboundPingPacket packet, CallbackInfo ci) {
        // act as S32PacketConfirmTransaction (aka ContainerAck)
        // since it's removed in 1.17 but converted to ping by ViaVersion
        // see ViaVersion's ClientboundPackets1_16_2.CONTAINER_ACK usage
        LifeSaverTimer.INSTANCE.whenServerTick();
    }

    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/common/custom/BrandPayload;brand()Ljava/lang/String;",
                    ordinal = 1))
    private void ghostify$handleBrandUpdate(ClientboundCustomPayloadPacket packet, CallbackInfo ci, @Local BrandPayload brandPayload) {
        String brand = brandPayload.brand();
        SkyblockLocation.whenServerBrandUpdate(brand);
        AutoTip.INSTANCE.whenServerBrandUpdate(brand);
    }
}
