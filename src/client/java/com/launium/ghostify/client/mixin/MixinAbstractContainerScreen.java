package com.launium.ghostify.client.mixin;

import com.launium.ghostify.client.feature.experimentation.AbstractExperimentSolver;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("unchecked")
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen {
    @WrapMethod(method = "slotClicked")
    private void ghostify$hijackSlotClick(Slot slot, int slotId, int mouseButton, ClickType type, Operation<Void> original) {
        if (slot != null && slot.index == 49 && AbstractExperimentSolver.ACTIVE_SOLVER != null) {
            if (Util.getMillis() - AbstractExperimentSolver.ACTIVE_SOLVER.startSolvingTimestamp < 300L) return;
            if (AbstractExperimentSolver.ACTIVE_SOLVER.willRedirectClick()) {
                slotId = AbstractExperimentSolver.ACTIVE_SOLVER.redirectedSlot();
                slot = ((AbstractContainerScreen<ChestMenu>) (Object) this).getMenu().getSlot(slotId);
                // use middle-click
                mouseButton = GLFW.GLFW_MOUSE_BUTTON_3;
                type = ClickType.CLONE;
            } else {
                return;
            }
        }
        original.call(slot, slotId, mouseButton, type);
    }
}
