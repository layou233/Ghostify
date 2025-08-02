package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.feature.DayViewer;
import com.launium.ghostify.client.feature.ForagingStyleWarning;
import com.launium.ghostify.client.feature.PickobulusPreview;
import com.launium.ghostify.client.ui.container.ServerTPSContainer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
    @Shadow
    public abstract void sendCommand(String command);

    @Inject(method = "handleSetTime", at = @At("TAIL"))
    private void ghostify$handleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        ServerTPSContainer.INSTANCE.whenClientboundSetTime();
        DayViewer.INSTANCE.whenClientboundSetTime();
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void ghostify$handleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        ServerTPSContainer.INSTANCE.whenRespawn();
        PickobulusPreview.INSTANCE.resetCooldown();
    }

    @Inject(method = "handleSoundEvent", at = @At("TAIL"))
    private void ghostify$handleSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        SoundEvent soundEvent = packet.getSound().value();
        if (SoundEvents.LADDER_BREAK.equals(soundEvent)) {
            ForagingStyleWarning.whenWoodBreakSound(packet);
        }
    }

    @Inject(method = "openCommandSendConfirmationWindow", at = @At("HEAD"), cancellable = true)
    private void ghostify$noCommandConfirmation(String command, String titleKey, Screen previousScreen, CallbackInfo ci) {
        if (ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION) {
            this.sendCommand(command);
            ci.cancel();
        }
    }
}
