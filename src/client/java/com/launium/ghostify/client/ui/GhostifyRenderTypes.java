package com.launium.ghostify.client.ui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class GhostifyRenderTypes {
    static final ShaderProgram SHADER_ROUND_RECT = new ShaderProgram(
            ResourceLocation.fromNamespaceAndPath("ghostify", "core/round_rect"),
            DefaultVertexFormat.POSITION,
            ShaderDefines.EMPTY
    );

    static final RenderStateShard.ShaderStateShard
            RENDERTYPE_ROUND_RECT = new RenderStateShard.ShaderStateShard(SHADER_ROUND_RECT);

    static final ImmutableList<RenderStateShard> ROUND_RECT_STATES = ImmutableList.of(
            RENDERTYPE_ROUND_RECT,
            NO_TEXTURE,
            TRANSLUCENT_TRANSPARENCY,
            //LEQUAL_DEPTH_TEST,
            NO_CULL,
            LIGHTMAP,
            NO_OVERLAY,
            NO_LAYERING,
            MAIN_TARGET,
            DEFAULT_TEXTURING,
            COLOR_DEPTH_WRITE,
            DEFAULT_LINE,
            NO_COLOR_LOGIC
    );

    static final RenderType
            ROUND_RECT = new RenderType("ghostify_round_rect", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS, RenderType.TRANSIENT_BUFFER_SIZE, false, false,
            () -> ROUND_RECT_STATES.forEach(RenderStateShard::setupRenderState),
            () -> ROUND_RECT_STATES.forEach(RenderStateShard::clearRenderState)) {
    };

    public static final RenderType.CompositeRenderType BOX_FILLED_NO_CULL = RenderType.create(
            "ghostify_box_filled_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP,
            RenderType.TRANSIENT_BUFFER_SIZE,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .createCompositeState(false));

    public static final RenderType.CompositeRenderType BOX_OUTLINE_NO_CULL = RenderType.create(
            "ghostify_box_outline_no_cull",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            RenderType.TRANSIENT_BUFFER_SIZE,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.of(3.0)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .createCompositeState(false));

    public static RenderType createTextureRenderType(DynamicTexture texture) {
        return RenderType.create("ghostify_dynamic_texture",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                RenderType.SMALL_BUFFER_SIZE,
                RenderType.CompositeState.builder()
                        .setTextureState(new DynamicTextureStateShard(texture, TriState.FALSE, false))
                        .setShaderState(POSITION_TEXTURE_COLOR_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .createCompositeState(false)
        );
    }
}
