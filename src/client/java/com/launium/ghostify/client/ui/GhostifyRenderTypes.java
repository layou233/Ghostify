package com.launium.ghostify.client.ui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GhostifyRenderTypes {
    static final RenderPipeline PIPELINE_ROUND_RECT = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("ghostify", "pipeline/round_rect"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("ghostify", "core/round_rect"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("ghostify", "core/round_rect"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withUniform("u", UniformType.UNIFORM_BUFFER)
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
                    .build()
    );

    static final RenderPipeline PIPELINE_DEBUG_TRIANGLE_STRIP = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("ghostify", "pipeline/debug_triangle_strip"))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .withCull(false)
                    .withUsePipelineDrawModeForGui(true) // by Fabric API
                    .build()
    );
}
