package com.launium.ghostify.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.Optional;

public class DynamicTextureStateShard extends RenderStateShard.EmptyTextureStateShard {
    private final DynamicTexture texture;
    private final TriState blur;
    private final boolean mipmap;

    public DynamicTextureStateShard(DynamicTexture texture, TriState blur, boolean mipmap) {
        super(() -> {
            texture.setFilter(blur, mipmap);
            RenderSystem.setShaderTexture(0, texture.getId());
        }, () -> {
        });
        this.texture = texture;
        this.blur = blur;
        this.mipmap = mipmap;
    }

    @Override
    public String toString() {
        return this.name + "[" + this.texture + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
    }

    @Override
    protected Optional<ResourceLocation> cutoutTexture() {
        return Optional.empty();
    }
}
