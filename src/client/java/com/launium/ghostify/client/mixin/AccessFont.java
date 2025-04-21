package com.launium.ghostify.client.mixin;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Font.class)
public interface AccessFont {
    @Accessor("splitter")
    StringSplitter getSplitter();
}
