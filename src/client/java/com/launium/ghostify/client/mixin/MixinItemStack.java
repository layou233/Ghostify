package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.interfaces.AccessItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements AccessItemStack {
    @Shadow
    public abstract Item getItem();

    @Unique
    private boolean ghostify$isShortbow = false;

    @Unique
    private boolean ghostify$hasPickobulusAbility = false;

    @Override
    public boolean ghostify$isShortbow() {
        return ghostify$isShortbow;
    }

    @Override
    public boolean ghostify$hasPickobulusAbility() {
        return ghostify$hasPickobulusAbility;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void ghostify$readItemComponents(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        List<Component> lines = components.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines();
        if (this.getItem() == Items.BOW) {
            this.ghostify$isShortbow = lines.stream().anyMatch(line -> line.getString().contains("Shortbow: Instantly shoots!"));
        } else {
            this.ghostify$hasPickobulusAbility = lines.stream().anyMatch(line -> line.getString().startsWith("Ability: Pickobulus"));
        }
    }
}
