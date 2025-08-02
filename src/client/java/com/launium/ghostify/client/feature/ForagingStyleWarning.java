package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.ui.container.ForagingStyleWarningContainer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class ForagingStyleWarning {
    private static final ResourceLocation HYPIXEL_GALATEA_BIOME = ResourceLocation.fromNamespaceAndPath("hypixel", "moonglade");

    public static void whenWoodBreakSound(ClientboundSoundPacket packet) {
        if (!ConfigManager.FEATURES.ENABLE_FORAGING_STYLE_WARNING) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (!client.player.getMainHandItem().getItem().getDescriptionId().endsWith("_axe")) {
            return;
        }
        BlockPos playerPos = client.player.getOnPos();
        Holder<Biome> biome = client.level.getBiomeFabric(playerPos);
        if (biome == null || !biome.is(HYPIXEL_GALATEA_BIOME)) return;

        float pitch = packet.getPitch();
        // note: the pitch of cutting the correct part of the wood is 1.0
        if (pitch != 0.2857143F) { // pitch of cutting the incorrect part
            return;
        }

        if (playerPos.distToLowCornerSqr(packet.getX(), packet.getY(), packet.getZ()) > 9F * 9F) {
            return;
        }

        ForagingStyleWarningContainer.INSTANCE.lastTriggeredTimestamp = Util.getMillis();
        GhostifyClient.island.show(ForagingStyleWarningContainer.INSTANCE);
    }
}
