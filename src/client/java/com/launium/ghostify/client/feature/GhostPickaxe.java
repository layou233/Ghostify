package com.launium.ghostify.client.feature;

import com.google.common.collect.Sets;
import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.container.GhostPickaxeContainer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

public class GhostPickaxe implements ClientTickEvents.StartTick {
    public static final GhostPickaxe INSTANCE = new GhostPickaxe();

    private static final KeyMapping GHOST_PICKAXE_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.ghost_pickaxe", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KeyMapping.CATEGORY_GAMEPLAY)
    );
    private static final GhostPickaxeContainer ghostPickaxeContainer = new GhostPickaxeContainer();
    private static final HashSet<Block> ignoreBlockSet = Sets.newHashSet(
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.LEVER
            //Blocks.PLAYER_WALL_HEAD, Blocks.PLAYER_HEAD
    );

    @Override
    public void onStartTick(Minecraft client) {
        if (client.player != null && GHOST_PICKAXE_KEY.isDown()) {
            ghostPickaxeContainer.isActivated = true;
            GhostifyClient.island.show(ghostPickaxeContainer);
            if (client.options.keyAttack.isDown()) {
                HitResult hitResult = client.player.pick(20F, 0, false);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                    BlockState state = client.level.getBlockState(pos);
                    if (!ignoreBlockSet.contains(state.getBlock())) {
                        client.level.removeBlock(pos, false);
                    }
                }
            }
        } else {
            ghostPickaxeContainer.isActivated = false;
        }
    }
}
