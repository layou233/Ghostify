package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.launium.ghostify.client.events.SimpleChatEventHandler;
import com.launium.ghostify.client.interfaces.AccessItemStack;
import com.launium.ghostify.client.ui.GhostifyRenderTypes;
import com.launium.ghostify.client.ui.container.PickobulusPreviewContainer;
import com.launium.ghostify.client.util.FadingColor;
import com.launium.ghostify.client.util.SwappingSlot;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class PickobulusPreview extends AbstractModule implements WorldRenderEvents.EndExtraction, WorldRenderEvents.AfterEntities, ClientTickEvents.EndTick, SimpleChatEventHandler.NonOverlay {
    public static final PickobulusPreview INSTANCE = new PickobulusPreview();
    private static final KeyMapping PICKOBULUS_PREVIEW_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.switch_pickobulus_preview", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, GhostifyClient.KEY_CATEGORY)
    );

    private boolean isEnabled = false;
    private boolean onCooldown = false;
    public final SwappingSlot<PickobulusPreviewContainer.State> stateSlot = new SwappingSlot<>(
            new PickobulusPreviewContainer.State(0, 0, 0, null),
            new PickobulusPreviewContainer.State(0, 0, 0, null)
    );
    public boolean isHoldingPickobulus = false;

    public void resetCooldown() {
        onCooldown = false;
    }

    private void checkPickobulusInHand(Minecraft client) {
        if (client.player == null) {
            isHoldingPickobulus = false;
            return;
        }
        ItemStack mainHandItem = client.player.getMainHandItem();
        isHoldingPickobulus = ((AccessItemStack) (Object) mainHandItem).ghostify$hasPickobulusAbility();
    }

    @SkipObfuscation
    @Override
    public void endExtraction(WorldExtractionContext context) {
        if (!isEnabled || !isHoldingPickobulus || onCooldown) {
            PickobulusPreviewContainer.INSTANCE.isActivated = false;
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        double eyeHeight = player.position().y + 0.53625F; // get base position first
        // 0.53625 is the Pickobulus magic value
        // then simulate 1.8 eye height
        if (player.isCrouching()) {
            eyeHeight += 1.54F;
        } else {
            eyeHeight += 1.62F;
        }
        Vec3 firePosition = new Vec3(player.getX(), eyeHeight, player.getZ());
        Vec3 viewVector = player.getViewVector(0F);
        Vec3 rayEnd = firePosition.add(viewVector.x * 30F, viewVector.y * 30F, viewVector.z * 30F);
        BlockHitResult hitResult = context.world().clip(new ClipContext(firePosition, rayEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            PickobulusPreviewContainer.State state = stateSlot.getBack();
            state.reset();
            state.bounds = new AABB(hitPos.getX() - 3F, hitPos.getY() - 3F, hitPos.getZ() - 3F,
                    hitPos.getX() + 3F, hitPos.getY() + 3F, hitPos.getZ() + 3F);
            context.world().getBlockStates(state.bounds.contract(1F, 1F, 1F)).forEach(blockState -> {
                Block block = blockState.getBlock();
                if (block != Blocks.AIR) {
                    state.blocks++;
                    String blockDescription = block.getDescriptionId();
                    if (blockDescription.endsWith("glass") || blockDescription.endsWith("glass_pane")) {
                        state.glasses++;
                    } else if (blockDescription.endsWith("ice")) {
                        state.ice++;
                    }
                }
            });
            stateSlot.swap();
            PickobulusPreviewContainer.INSTANCE.isActivated = true;
            GhostifyClient.island.show(PickobulusPreviewContainer.INSTANCE);
        } else {
            PickobulusPreviewContainer.INSTANCE.isActivated = false;
        }
    }

    @SkipObfuscation
    @Override
    public void afterEntities(WorldRenderContext context) {
        if (PickobulusPreviewContainer.INSTANCE.isActivated) {
            int color = FadingColor.pink(3, 0, 0xFF);
            AABB bounds = stateSlot.get().bounds;
            if (bounds == null) return;
            Gizmos.cuboid(bounds, new GizmoStyle(color, 3F, ARGB.multiplyAlpha(color, 0.2F)));
        }
    }

    @Override
    public void onEndTick(Minecraft client) {
        checkPickobulusInHand(client);
        while (PICKOBULUS_PREVIEW_KEY.consumeClick()) {
            isEnabled = !isEnabled;
            if (isEnabled) {
                onCooldown = false; // force reset
                GhostifyClient.moduleList.showModule(this);
            } else {
                PickobulusPreviewContainer.INSTANCE.isActivated = true;
            }
        }
    }

    @Override
    public String title() {
        return "PickobulusPreview";
    }

    @Override
    public @Nullable String subtitle() {
        return onCooldown ? "CD" : "OK";
    }

    @Override
    public boolean isActive() {
        return isEnabled;
    }

    @Override
    public void onReceiveChat(String text) {
        if (!isEnabled) return;
        if ("You used your Pickobulus Pickaxe Ability!".equals(text) ||
                text.startsWith("Your Pickaxe ability is on cooldown for ")) {
            onCooldown = true;
        } else if ("Pickobulus is now available!".equals(text)) {
            onCooldown = false;
        }
    }
}
