package com.launium.ghostify.client.ui;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;

public class RoundRectRenderState implements GuiElementRenderState {
    public final static Int2ObjectArrayMap<RoundRectRenderState> RESTORE = new Int2ObjectArrayMap<>(8);
    private final static float OUTSET = 14F; // conservative

    public final float left, top, right, bottom;
    public final float depth;
    public final float shadow;
    public final Vector4f color, shadowColor; // normalized RGBA
    public float radiusRB, radiusRT, radiusLB, radiusLT; // left-right; top-bottom
    public @Nullable Vector4f color2;
    public float gradiantDirectionX = 0F, gradiantDirectionY = 0F;
    public float edgeSoftness = 1F;
    public GpuBufferSlice uniformBuffer;

    private final float centerX, centerY, extentX, extentY;
    private final GuiGraphics context;
    private final ScreenRectangle bounds;
    private final TextureSetup textureSetup; // used as reference for restoring

    public RoundRectRenderState(GuiGraphics context, float left, float top, float right, float bottom,
                                float depth, float radius, float shadow, int color, int shadowColor) {
        if (!Float.isFinite(radius) || radius < 0.0f) { // NaN, Inf, negative
            radius = 0;
        }

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.depth = depth;
        this.shadow = shadow;
        this.color = new Vector4f(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
        this.shadowColor = new Vector4f(ARGB.red(shadowColor) / 255F, ARGB.green(shadowColor) / 255F, ARGB.blue(shadowColor) / 255F, ARGB.alpha(shadowColor) / 255F);

        this.centerX = (left + right) * 0.5f;
        this.centerY = (top + bottom) * 0.5f;
        this.extentX = right - left;
        this.extentY = bottom - top;
        this.radiusLT = this.radiusRT = this.radiusLB = this.radiusRB = Math.min(radius, Math.min(extentX, extentY) * 0.5F);
        this.context = context;
        this.bounds = new ScreenRectangle((int) Math.floor(left), (int) Math.floor(top), (int) Math.ceil(extentX), (int) Math.ceil(extentY));

        this.textureSetup = TextureSetup.singleTexture(null);
        RESTORE.put(System.identityHashCode(this.textureSetup), this);
    }

    public RoundRectRenderState setGradiantColor(int color) {
        this.color2 = new Vector4f(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
        return this;
    }

    public RoundRectRenderState setGradiantDirection(float x, float y) {
        this.gradiantDirectionX = x;
        this.gradiantDirectionY = y;
        return this;
    }

    public RoundRectRenderState setEdgeSoftness(float edgeSoftness) {
        this.edgeSoftness = edgeSoftness;
        return this;
    }

    @Override
    public void buildVertices(VertexConsumer consumer, float z) {
        Matrix3x2f pose = context.pose();
        consumer.addVertexWith2DPose(pose, left - OUTSET, top - OUTSET, z);
        consumer.addVertexWith2DPose(pose, left - OUTSET, bottom + OUTSET, z);
        consumer.addVertexWith2DPose(pose, right + OUTSET, bottom + OUTSET, z);
        consumer.addVertexWith2DPose(pose, right + OUTSET, top - OUTSET, z);
        this.uniformBuffer = Uniforms.storage.writeUniform(buffer -> {
            Std140Builder.intoBuffer(buffer)
                    .putVec4(this.centerX, this.centerY, this.extentX, this.extentY) // u_Rect
                    .putVec4(this.radiusRB, this.radiusRT, this.radiusLB, this.radiusLT) // u_Radii
                    .putVec4(this.color) // u_colorRect
                    .putVec4(this.color2 == null ? this.color : this.color2) // u_colorRect2
                    .putVec4(this.shadowColor) // u_colorShadow
                    .putVec2(this.gradiantDirectionX, this.gradiantDirectionY) // u_gradientDirectionVector
                    .putFloat(this.edgeSoftness) // u_edgeSoftness
                    .putFloat(this.shadow); // u_shadowSoftness
        });
    }

    @Override
    public @NotNull RenderPipeline pipeline() {
        return GhostifyRenderTypes.PIPELINE_ROUND_RECT;
    }

    @Override
    public @NotNull TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return context.scissorStack.peek();
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        ScreenRectangle scissorArea = this.scissorArea();
        return scissorArea == null ? bounds : scissorArea.intersection(bounds);
    }

    public static class Uniforms {
        public static final int SIZE = new Std140SizeCalculator()
                .putVec4() // u_Rect
                .putVec4() // u_Radii
                .putVec4() // u_colorRect
                .putVec4() // u_colorRect2
                .putVec4() // u_colorShadow
                .putVec2() // u_gradientDirectionVector
                .putFloat() // u_edgeSoftness
                .putFloat() // u_shadowSoftness
                .get();
        private static final DynamicUniformStorage<DynamicUniformStorage.DynamicUniform> storage = new DynamicUniformStorage<>("Ghostify Rounded Rectangle UBO", SIZE, 4);

        public static void clear() {
            storage.endFrame();
        }
    }
}
