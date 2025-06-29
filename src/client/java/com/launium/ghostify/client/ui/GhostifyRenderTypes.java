package com.launium.ghostify.client.ui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class GhostifyRenderTypes {
    static final RenderPipeline PIPELINE_ROUND_RECT = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(ResourceLocation.fromNamespaceAndPath("ghostify", "pipeline/round_rect"))
                    .withFragmentShader(ResourceLocation.fromNamespaceAndPath("ghostify", "core/round_rect"))
                    .withVertexShader(ResourceLocation.fromNamespaceAndPath("ghostify", "core/round_rect"))
                    //.withColorWrite(true)
                    .withDepthWrite(true)
                    .withCull(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withColorLogic(LogicOp.NONE)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("u", UniformType.UNIFORM_BUFFER)
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
                    .build()
    );

    static final RenderType.CompositeRenderType
            ROUND_RECT = RenderType.create("ghostify_round_rect", RenderType.TRANSIENT_BUFFER_SIZE,
            false, false, PIPELINE_ROUND_RECT,
            RenderType.CompositeState.builder()
                    .setTextureState(NO_TEXTURE)
                    .setLayeringState(NO_LAYERING)
                    .setLightmapState(LIGHTMAP)
                    .setOutputState(MAIN_TARGET)
                    .setOverlayState(NO_OVERLAY)
                    .setTexturingState(DEFAULT_TEXTURING)
                    .setLineState(DEFAULT_LINE)
                    .createCompositeState(false));

    static final RenderPipeline PIPELINE_BOX_FILLED_NO_CULL = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(ResourceLocation.fromNamespaceAndPath("ghostify", "pipeline/box_filled_no_cull"))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );

    public static final RenderType.CompositeRenderType BOX_FILLED_NO_CULL = RenderType.create(
            "ghostify_box_filled_no_cull",
            RenderType.TRANSIENT_BUFFER_SIZE,
            false, false, PIPELINE_BOX_FILLED_NO_CULL,
            RenderType.CompositeState.builder()
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false));

    static final RenderPipeline PIPELINE_BOX_OUTLINE_NO_CULL = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(ResourceLocation.fromNamespaceAndPath("ghostify", "pipeline/box_outline_no_cull"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );

    public static final RenderType.CompositeRenderType BOX_OUTLINE_NO_CULL = RenderType.create(
            "ghostify_box_outline_no_cull",
            RenderType.TRANSIENT_BUFFER_SIZE,
            false, false, PIPELINE_BOX_OUTLINE_NO_CULL,
            RenderType.CompositeState.builder()
                    .setLineState(new LineStateShard(OptionalDouble.of(3.0))) // line width
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false));
}
