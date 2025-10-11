package com.launium.ghostify.client.feature;

import com.google.common.collect.Sets;
import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.ui.container.GhostPickaxeContainer;
import com.launium.ghostify.client.util.SkyblockLocation;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

public class GhostPickaxe extends AbstractModule implements ClientTickEvents.StartTick {
    public static final GhostPickaxe INSTANCE = new GhostPickaxe();

    private static final KeyMapping GHOST_PICKAXE_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.ghost_pickaxe", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, GhostifyClient.KEY_CATEGORY)
    );
    private static final HashSet<Block> ignoreBlockSet = Sets.newHashSet(
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.LEVER
            //Blocks.PLAYER_WALL_HEAD, Blocks.PLAYER_HEAD
    );

    private static int lastSlot = -1;

    @Override
    public void onStartTick(Minecraft client) {
        if (client.player != null && GHOST_PICKAXE_KEY.isDown()) {
            boolean enable = true;
            if (BetterDungeonbreaker.slot > 0 && SkyblockLocation.isInDungeons()) { // found Dungeonbreaker in hotbar
                if (lastSlot < 0) { // not swapped yet
                    GhostPickaxeContainer.INSTANCE.isLegacy = false;
                    Inventory inventory = client.player.getInventory();
                    lastSlot = inventory.getSelectedSlot();
                    inventory.setSelectedSlot(BetterDungeonbreaker.slot);
                }
            } else if (ConfigManager.FEATURES.ENABLE_LEGACY_GHOST_PICKAXE) { // legacy mode
                GhostPickaxeContainer.INSTANCE.isLegacy = true;
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
                enable = false;
            }
            if (enable) {
                GhostPickaxeContainer.INSTANCE.isActivated = true;
                GhostifyClient.island.show(GhostPickaxeContainer.INSTANCE);
                GhostifyClient.moduleList.showModule(this);
            }
        } else {
            if (client.player != null && lastSlot >= 0) {
                client.player.getInventory().setSelectedSlot(lastSlot);
                lastSlot = -1;
            }
            GhostPickaxeContainer.INSTANCE.isActivated = false;
        }
    }

    @Override
    public String title() {
        return "GhostPickaxe";
    }

    @Override
    public @Nullable String subtitle() {
        return null;
    }

    @Override
    public boolean isActive() {
        return GhostPickaxeContainer.INSTANCE.isActivated;
    }
}
