package com.launium.ghostify.client.ui;

import com.launium.ghostify.client.mixin.AccessFont;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Easy2D {
    private static GuiGraphics context;
    private static final CachedOrthoProjectionMatrixBuffer guiProjectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("gui", 1000.0F, 11000.0F, true);

    public static void configure(GuiGraphics newContext) {
        context = newContext;
    }

    public static void cleanup() {
        context = null;
    }

    private static final int roundRectBufferSize = new Std140SizeCalculator()
            .putVec4() // u_Rect
            .putVec4() // u_Radii
            .putVec4() // u_colorRect
            .putVec4() // u_colorRect2
            .putVec4() // u_colorShadow
            .putVec2() // u_gradientDirectionVector
            .putFloat() // u_edgeSoftness
            .putFloat() // u_shadowSoftness
            .get();

//    private static final MappableRingBuffer roundRectUBO = new MappableRingBuffer(
//            () -> "Ghostify Round Rect UBO",
//            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
//            new Std140SizeCalculator()
//                    .putVec4() // u_Rect
//                    .putVec4() // u_Radii
//                    .putVec4() // u_colorRect
//                    .putVec4() // u_colorRect2
//                    .putVec4() // u_colorShadow
//                    .putVec2() // u_gradientDirectionVector
//                    .putFloat() // u_edgeSoftness
//                    .putFloat() // u_shadowSoftness
//                    .get()
//    );

    public static void drawRoundRect(float left, float top, float right, float bottom,
                                     float depth, float radius, float shadow, int color, int shadowColor) {
        if (!(left < right && top < bottom)) { // also capture NaN
            return;
        }
        if (!Float.isFinite(radius) || radius < 0.0f) { // NaN, Inf, negative
            radius = 0;
        }
        Matrix3x2f pose = context.pose();
        float centerX = (left + right) * 0.5f;
        float centerY = (top + bottom) * 0.5f;
        float extentX = right - left;
        float extentY = bottom - top;
        radius = Math.min(radius, Math.min(extentX, extentY) * 0.5F);
        final float outset = 14F; // conservative
        // build vertex buffer
        try (ByteBufferBuilder allocator = new ByteBufferBuilder(DefaultVertexFormat.POSITION.getVertexSize() * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(allocator, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferBuilder.addVertexWith2DPose(pose, left - outset, top - outset, depth);
            bufferBuilder.addVertexWith2DPose(pose, left - outset, bottom + outset, depth);
            bufferBuilder.addVertexWith2DPose(pose, right + outset, bottom + outset, depth);
            bufferBuilder.addVertexWith2DPose(pose, right + outset, top - outset, depth);
            try (MeshData mesh = bufferBuilder.build()) {
                // build uniform buffer
                //roundRectUBO.rotate();
                GpuBuffer uniformBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Ghostify Round Rect UBO",
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, roundRectBufferSize
                );
                Vector4f colorVector = new Vector4f((float) ARGB.red(color) / 255F, (float) ARGB.green(color) / 255F, (float) ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
                try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(uniformBuffer, false, true)) { // false, true means write only
                    Std140Builder.intoBuffer(view.data())
                            .putVec4(centerX, centerY, extentX, extentY) // u_Rect
                            .putVec4(radius, radius, radius, radius) // u_Radii
                            .putVec4(colorVector) // u_colorRect
                            .putVec4(colorVector) // u_colorRect2
                            .putVec4(ARGB.red(shadowColor) / 255F, ARGB.green(shadowColor) / 255F, ARGB.blue(shadowColor) / 255F, ARGB.alpha(shadowColor) / 255F) // u_colorShadow
                            .putVec2(0F, 0F) // u_gradientDirectionVector
                            .putFloat(1F) // u_edgeSoftness
                            .putFloat(shadow); // u_shadowSoftness
                }

                Window window = Minecraft.getInstance().getWindow();
                GhostifyRenderTypes.ROUND_RECT.setupRenderState();
                RenderSystem.setProjectionMatrix(
                        guiProjectionMatrixBuffer.getBuffer((float) window.getWidth() / window.getGuiScale(), (float) window.getHeight() / window.getGuiScale()),
                        ProjectionType.ORTHOGRAPHIC
                );
                GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        colorVector,
                        RenderSystem.getModelOffset(),
                        RenderSystem.getTextureMatrix(),
                        RenderSystem.getShaderLineWidth());
                GpuBuffer vertexBuffer = DefaultVertexFormat.POSITION.uploadImmediateVertexBuffer(mesh.vertexBuffer());
                RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                GpuBuffer indexBuffer = shapeIndexBuffer.getBuffer(mesh.drawState().indexCount());
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "Ghostify Round Rect Pass",
                        RenderSystem.outputColorTextureOverride == null ? RenderStateShard.MAIN_TARGET.getRenderTarget().getColorTextureView() : RenderSystem.outputColorTextureOverride,
                        OptionalInt.empty(),
                        RenderSystem.outputDepthTextureOverride == null ? RenderStateShard.MAIN_TARGET.getRenderTarget().getDepthTextureView() : RenderSystem.outputDepthTextureOverride,
                        OptionalDouble.empty()
                )) {
                    renderPass.setPipeline(GhostifyRenderTypes.PIPELINE_ROUND_RECT);
                    renderPass.setVertexBuffer(0, vertexBuffer);
                    renderPass.setIndexBuffer(indexBuffer, shapeIndexBuffer.type());
                    renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                    renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                    renderPass.setUniform("u", uniformBuffer);
                    renderPass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
                }
                GhostifyRenderTypes.ROUND_RECT.clearRenderState();
                uniformBuffer.close();
            }
        }
        // we modify uniform for each draw, so cannot do batch rendering
        //context.flush();
    }

    public static final int TEXT_DEFAULT_COLOR = -1;

    public static void drawScreenText(Font font, String text, float x, float y, int color, boolean shadow) {
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(x, y);
        context.drawString(font, text, 0, 0, color, shadow);
        pose.popMatrix();
    }

    public static void drawScreenTextsCentered(Font font, float x, float y, int color, boolean shadow, String... lines) {
        if (lines.length == 0) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = y - font.lineHeight * lines.length * 0.5F;
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(0f, 0f);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            pose.setTranslation(x - splitter.stringWidth(line) * 0.5F, startY + font.lineHeight * i);
            context.drawString(font, line, 0, 0, color, shadow);
        }
        pose.popMatrix();
    }

    public static void drawScreenTextElements(Font font, float startX, float endX, float centerY, boolean shadow, VanillaText... elements) {
        if (elements.length == 0) return;
        StringSplitter splitter = ((AccessFont) font).getSplitter();
        float startY = centerY - font.lineHeight * elements.length * 0.5F;
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(0f, 0f);
        for (int i = 0; i < elements.length; i++) {
            TextElement element = elements[i];
            pose.setTranslation(element.align.calculate(startX, endX, splitter.stringWidth(element.text)),
                    startY + font.lineHeight * i);
            context.drawString(font, element.text, 0, 0, element.color, shadow);
        }
        pose.popMatrix();
    }
}
