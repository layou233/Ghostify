package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.GhostifyRenderTypes;
import com.launium.ghostify.client.ui.container.PickobulusPreviewContainer;
import com.launium.ghostify.client.util.FadingColor;
import com.launium.ghostify.client.util.Remember;
import com.launium.ghostify.client.util.SwappingSlot;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class PickobulusPreview extends AbstractModule implements WorldRenderEvents.AfterEntities, ClientTickEvents.EndTick, ClientReceiveMessageEvents.Game {
    public static final PickobulusPreview INSTANCE = new PickobulusPreview();
    private static final KeyMapping PICKOBULUS_PREVIEW_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.switch_pickobulus_preview", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, GhostifyClient.KEY_CATEGORY)
    );

    private boolean isEnabled = false;
    private boolean onCooldown = false;
    private final Remember<Integer> rememberItemComponentsHash = new Remember<>(null);
    public final SwappingSlot<PickobulusPreviewContainer.Stat> statSlot = new SwappingSlot<>(
            new PickobulusPreviewContainer.Stat(0, 0, 0),
            new PickobulusPreviewContainer.Stat(0, 0, 0)
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
        DataComponentMap componentMap = mainHandItem.getComponents();
        if (rememberItemComponentsHash.updateObject(componentMap.hashCode())) {
            // fast path; item in hand is not changed
            // only store the hash to avoid the reference to the object
            return;
        }
        isHoldingPickobulus = componentMap.getOrDefault(DataComponents.LORE, ItemLore.EMPTY)
                .styledLines().stream()
                .anyMatch(line -> line.getString().startsWith("Ability: Pickobulus"));
    }

    @Override
    public void afterEntities(WorldRenderContext context) {
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
            AABB bounds = new AABB(hitPos.getX() - 3F, hitPos.getY() - 3F, hitPos.getZ() - 3F,
                    hitPos.getX() + 3F, hitPos.getY() + 3F, hitPos.getZ() + 3F);
            PickobulusPreviewContainer.Stat stat = statSlot.getBack();
            stat.reset();
            context.world().getBlockStates(bounds.contract(1F, 1F, 1F)).forEach(blockState -> {
                Block block = blockState.getBlock();
                if (block != Blocks.AIR) {
                    stat.blocks++;
                    String blockDescription = block.getDescriptionId();
                    if (blockDescription.endsWith("glass") || blockDescription.endsWith("glass_pane")) {
                        stat.glasses++;
                    } else if (blockDescription.endsWith("ice")) {
                        stat.ice++;
                    }
                }
            });
            statSlot.swap();
            PickobulusPreviewContainer.INSTANCE.isActivated = true;
            GhostifyClient.island.show(PickobulusPreviewContainer.INSTANCE);
            int color = FadingColor.pink(3, 0, 0xFF);
            AABB transformedBounds = bounds.move(context.camera().getPosition().reverse());
            VertexConsumer buffer = context.consumers().getBuffer(GhostifyRenderTypes.BOX_FILLED_NO_CULL);
            ShapeRenderer.addChainedFilledBoxVertices(context.matrixStack(), buffer,
                    transformedBounds.minX, transformedBounds.minY, transformedBounds.minZ,
                    transformedBounds.maxX, transformedBounds.maxY, transformedBounds.maxZ,
                    ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, 0.2F);
            buffer = context.consumers().getBuffer(GhostifyRenderTypes.BOX_OUTLINE_NO_CULL);
            ShapeRenderer.renderLineBox(context.matrixStack(), buffer, transformedBounds,
                    ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, 1.0F);
        } else {
            PickobulusPreviewContainer.INSTANCE.isActivated = false;
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
    public void onReceiveGameMessage(Component message, boolean isOverlay) {
        if (isOverlay || !isEnabled) return;
        String text = message.getString();
        if ("You used your Pickobulus Pickaxe Ability!".equals(text) ||
                text.startsWith("Your pickaxe ability is on cooldown for ")) {
            onCooldown = true;
        } else if ("Pickobulus is now available!".equals(text)) {
            onCooldown = false;
        }
    }
}
