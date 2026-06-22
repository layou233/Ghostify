package com.launium.ghostify.client.ui;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GhostifyRenderTypes {
    private static final BindGroupLayout LAYOUT_ROUND_RECT = BindGroupLayout.builder()
            .withUniform("u", UniformType.UNIFORM_BUFFER)
            .build();

    static final RenderPipeline PIPELINE_ROUND_RECT = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("ghostify", "pipeline/round_rect"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("ghostify", "core/round_rect"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("ghostify", "core/round_rect"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withBindGroupLayout(LAYOUT_ROUND_RECT)
                    .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    .build()
    );

    static final RenderPipeline PIPELINE_DEBUG_TRIANGLE_STRIP = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("ghostify", "pipeline/debug_triangle_strip"))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
                    .withCull(false)
                    .withUsePipelineDrawModeForGui(true) // by Fabric API
                    .build()
    );
}
