package com.launium.ghostify.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AccessAbstractContainerScreen {
    @Invoker("slotClicked")
    void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);
}
