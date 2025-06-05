package com.launium.ghostify.client.ui;

import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.mixin.AccessGuiGraphics;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Easy2D {
    private static GuiGraphics context;

    public static void configure(GuiGraphics newContext) {
        context = newContext;
    }

    public static void cleanup() {
        context = null;
    }

    public static void drawRoundRect(float left, float top, float right, float bottom,
                                     float depth, float radius, float shadow, int color) {
        if (!(left < right && top < bottom)) { // also capture NaN
            return;
        }
        if (!Float.isFinite(radius) || radius < 0.0f) { // NaN, Inf, negative
            radius = 0;
        }
        Matrix4f pose = context.pose().last().pose();
        float centerX = (left + right) * 0.5f;
        float centerY = (top + bottom) * 0.5f;
        float extentX = right - left;
        float extentY = bottom - top;
        radius = Math.min(radius, Math.min(extentX, extentY) * 0.5F);
        final float outset = 14F; // conservative
        try (ByteBufferBuilder allocator = new ByteBufferBuilder(DefaultVertexFormat.POSITION.getVertexSize() * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(allocator, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferBuilder.addVertex(pose, left - outset, top - outset, depth);
            bufferBuilder.addVertex(pose, left - outset, bottom + outset, depth);
            bufferBuilder.addVertex(pose, right + outset, bottom + outset, depth);
            bufferBuilder.addVertex(pose, right + outset, top - outset, depth);
            boolean isPureTranslation = (pose.properties() & Matrix4f.PROPERTY_TRANSLATION) != 0;
            if (!isPureTranslation) {
                // here we modify global model view, so cannot do batch rendering
                context.flush();
            }
            try (MeshData mesh = bufferBuilder.build()) {
                GpuBuffer vertexBuffer = DefaultVertexFormat.POSITION.uploadImmediateVertexBuffer(mesh.vertexBuffer());
                GpuBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(mesh.drawState().indexCount());
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        GhostifyRenderTypes.ROUND_RECT.getRenderTarget().getColorTexture(),
                        OptionalInt.empty(),
                        GhostifyRenderTypes.ROUND_RECT.getRenderTarget().getDepthTexture(),
                        OptionalDouble.empty()
                )) {
                    renderPass.setPipeline(GhostifyRenderTypes.PIPELINE_ROUND_RECT);
                    renderPass.setVertexBuffer(0, vertexBuffer);
                    renderPass.setIndexBuffer(indexBuffer, RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).type());
                    if (isPureTranslation) {
                        // fast path
                        renderPass.setUniform("u_Rect",
                                centerX + pose.m30(), centerY + pose.m31(), extentX, extentY);
                    } else {
                        // we expect local coordinates, concat pose with model view
                        RenderSystem.getModelViewStack().pushMatrix();
                        RenderSystem.getModelViewStack().mul(pose);
                        renderPass.setUniform("u_Rect",
                                centerX, centerY, extentX, extentY);
                    }
                    renderPass.setUniform("ModelViewMat", RenderSystem.getModelViewMatrix());
                    renderPass.setUniform("ProjMat", RenderSystem.getProjectionMatrix());
                    renderPass.setUniform("u_Radii", radius, radius, radius, radius);
                    float[] colorVector = new float[]{(float) ARGB.red(color) / 255F, (float) ARGB.green(color) / 255F, (float) ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F};
                    renderPass.setUniform("u_colorRect", colorVector);
                    renderPass.setUniform("u_colorRect2", colorVector);
                    renderPass.setUniform("u_colorShadow", 0F, 0F, 0F, ARGB.alpha(color) / 255F);
                    renderPass.setUniform("u_edgeSoftness", 1F);
                    renderPass.setUniform("u_shadowSoftness", shadow);
                    renderPass.drawIndexed(0, mesh.drawState().indexCount());

                    if (!isPureTranslation)
                        RenderSystem.getModelViewStack().popMatrix();
                }
            }
        }
        // we modify uniform for each draw, so cannot do batch rendering
        context.flush();
    }

    public static final int TEXT_DEFAULT_COLOR = -1;

    public static void drawScreenText(Font font, String text, float x, float y, int color, boolean shadow) {
        font.drawInBatch(text, x, y, color, shadow, context.pose().last().pose(),
                ((AccessGuiGraphics) context).getBufferSource(), Font.DisplayMode.SEE_THROUGH, 0, Integer.MAX_VALUE);
    }

    public static void drawScreenTextsCentered(Font font, float x, float y, int color, boolean shadow, String... lines) {
        if (lines.length == 0) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = y - font.lineHeight * lines.length * 0.5F;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            drawScreenText(font, line,
                    x - splitter.stringWidth(line) * 0.5F, startY + font.lineHeight * i,
                    color, shadow);
        }
    }

    public static void drawScreenTextElements(Font font, float startX, float endX, float centerY, boolean shadow, TextElement... elements) {
        if (elements.length == 0) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = centerY - font.lineHeight * elements.length * 0.5F;
        for (int i = 0; i < elements.length; i++) {
            TextElement element = elements[i];
            drawScreenText(font, element.text,
                    element.align.calculate(startX, endX, splitter.stringWidth(element.text)),
                    startY + font.lineHeight * i,
                    element.color, shadow
            );
        }
    }
}
