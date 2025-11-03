package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.util.SkyblockLocation;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DungeonPlaceFix implements UseBlockCallback {
    public static final DungeonPlaceFix INSTANCE = new DungeonPlaceFix();

    private static InteractionResult useItemOn(UseOnContext context) {
        // from MultiPlayerGameMode.performUseItemOn
        ItemStack itemStack = context.getPlayer().getItemInHand(context.getHand());
        if (context.getPlayer().hasInfiniteMaterials()) {
            int i = itemStack.getCount();
            InteractionResult result = itemStack.useOn(context);
            itemStack.setCount(i);
            return result;
        } else {
            return itemStack.useOn(context);
        }
    }

    @SkipObfuscation
    @Override
    public InteractionResult interact(Player player, Level level, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (ConfigManager.PATCHES.FIX_DUNGEON_BLOCK_PLACE && SkyblockLocation.isInDungeons()) {
            BlockState blockState = level.getBlockState(blockHitResult.getBlockPos());
            if (blockState.getBlock() == Blocks.ENCHANTING_TABLE && blockHitResult.getDirection() != Direction.UP) {
                return useItemOn(new UseOnContext(player, interactionHand, blockHitResult));
            }
        }
        return InteractionResult.PASS;
    }
}
