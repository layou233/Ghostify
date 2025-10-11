package com.launium.ghostify.client.ui;

import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.vertex.*;
import lombok.EqualsAndHashCode;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class RoundRectRenderer extends PictureInPictureRenderer<RoundRectRenderer.State> {
    private State lastState;

    public static void init() {
        SpecialGuiElementRegistry.register(context -> new RoundRectRenderer(context.vertexConsumers()));
    }

    protected RoundRectRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @SkipObfuscation
    @Override
    public @NotNull Class<State> getRenderStateClass() {
        return State.class;
    }

    @SkipObfuscation
    @Override
    protected boolean textureIsReadyToBlit(State state) {
        return Objects.equals(lastState, state);
    }

    @SkipObfuscation
    @Override
    protected void renderToTexture(State state, PoseStack poseStack) {
        float width = (state.extentX + 2 * State.OUTSET) * state.scale;
        float height = (state.extentY + 2 * State.OUTSET) * state.scale;

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        {
            float textureWidth = width + state.subpixelX * state.scale;
            float textureHeight = height + state.subpixelY * state.scale;
            builder.addVertex(0F, 0F, state.depth);
            builder.addVertex(0F, textureHeight, state.depth);
            builder.addVertex(textureWidth, textureHeight, state.depth);
            builder.addVertex(textureWidth, 0, state.depth);
        }
        MeshData mesh = builder.buildOrThrow();

        GpuBufferSlice dynamicTransformsBuffer = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                new Vector4f(),
                new Vector3f(),
                RenderSystem.getTextureMatrix(),
                RenderSystem.getShaderLineWidth()
        );
        GpuBufferSlice myUniformBuffer = Uniform.STORAGE.writeUniform(buffer -> {
            Std140Builder.intoBuffer(buffer)
                    .putVec4(state.subpixelX * state.scale + width * 0.5F, state.subpixelY * state.scale + height * 0.5F, state.extentX * state.scale, state.extentY * state.scale) // u_Rect
                    .putVec4(state.radiusRB * state.scale, state.radiusRT * state.scale, state.radiusLB * state.scale, state.radiusLT * state.scale) // u_Radii
                    .putVec4(state.color) // u_colorRect
                    .putVec4(state.color2 == null ? state.color : state.color2) // u_colorRect2
                    .putVec4(state.shadowColor) // u_colorShadow
                    .putVec2(state.gradiantDirectionX, state.gradiantDirectionY) // u_gradientDirectionVector
                    .putFloat(state.edgeSoftness * state.scale) // u_edgeSoftness
                    .putFloat(state.shadow * state.scale); // u_shadowSoftness
        });
        GpuBuffer vertexBuffer = GhostifyRenderTypes.PIPELINE_ROUND_RECT.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
        RenderSystem.AutoStorageIndexBuffer indexStorage = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
        GpuBuffer indexBuffer = indexStorage.getBuffer(mesh.drawState().indexCount());
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        try (
                mesh;
                RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "Ghostify Rounded Rectangle Render Pass",
                        Objects.requireNonNullElse(RenderSystem.outputColorTextureOverride, renderTarget.getColorTextureView()),
                        OptionalInt.empty(),
                        renderTarget.useDepth ? Objects.requireNonNullElse(RenderSystem.outputDepthTextureOverride, renderTarget.getDepthTextureView()) : null,
                        OptionalDouble.empty()
                )
        ) {
            pass.setPipeline(GhostifyRenderTypes.PIPELINE_ROUND_RECT);
            ScissorState scissor = RenderSystem.getScissorStateForRenderTypeDraws();
            if (scissor.enabled()) {
                pass.enableScissor(scissor.x(), scissor.y(), scissor.width(), scissor.height());
            }
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransformsBuffer);
            pass.setUniform("u", myUniformBuffer);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, indexStorage.type());
            pass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
        }

        lastState = state;
    }

    @SkipObfuscation
    @Override
    protected @NotNull String getTextureLabel() {
        return "Ghostify Rounded Rectangle PIP";
    }

    @EqualsAndHashCode
    public static class State implements PictureInPictureRenderState {
        public final static float OUTSET = 14F; // conservative

        public transient final float left, top, right, bottom;
        public final float depth, scale;
        public final float shadow;
        public final Vector4f color, shadowColor; // normalized RGBA
        public @Nullable Vector4f color2;
        public float radiusRB, radiusRT, radiusLB, radiusLT; // left-right; top-bottom
        public float gradiantDirectionX = 0F, gradiantDirectionY = 0F;
        public float edgeSoftness = 1F;

        private final float subpixelX, subpixelY; // pad texture for subpixel rendering
        private final float extentX, extentY;
        private transient final GuiGraphics context;
        private transient final ScreenRectangle bounds;

        public State(GuiGraphics context, float left, float top, float right, float bottom,
                     float depth, float radius, float shadow, int color, int shadowColor) {
            if (!Float.isFinite(radius) || radius < 0.0f) { // NaN, Inf, negative
                radius = 0;
            }

            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.subpixelX = left - Mth.floor(left);
            this.subpixelY = top - Mth.floor(top);
            this.depth = depth;
            this.scale = Minecraft.getInstance().getWindow().getGuiScale();
            this.shadow = shadow;
            this.color = new Vector4f(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
            this.shadowColor = new Vector4f(ARGB.red(shadowColor) / 255F, ARGB.green(shadowColor) / 255F, ARGB.blue(shadowColor) / 255F, ARGB.alpha(shadowColor) / 255F);

            this.extentX = right - left;
            this.extentY = bottom - top;
            this.radiusLT = this.radiusRT = this.radiusLB = this.radiusRB = Math.min(radius, Math.min(extentX, extentY) * 0.5F);
            this.context = context;
            this.bounds = new ScreenRectangle(this.x0(), this.y0(), this.x1() - this.x0(), this.y1() - this.y0());
        }

        public State setGradiantColor(int color) {
            this.color2 = new Vector4f(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
            return this;
        }

        public State setGradiantDirection(float x, float y) {
            this.gradiantDirectionX = x;
            this.gradiantDirectionY = y;
            return this;
        }

        public State setEdgeSoftness(float edgeSoftness) {
            this.edgeSoftness = edgeSoftness;
            return this;
        }

        @SkipObfuscation
        @Override
        public int x0() {
            return Mth.floor(left) - (int) OUTSET;
        }

        @SkipObfuscation
        @Override
        public int x1() {
            return Mth.ceil(right + OUTSET);
        }

        @SkipObfuscation
        @Override
        public int y0() {
            return Mth.floor(top) - (int) OUTSET;
        }

        @SkipObfuscation
        @Override
        public int y1() {
            return Mth.ceil(bottom + OUTSET);
        }

        @SkipObfuscation
        @Override
        public float scale() {
            return 1F;
        }

        @SkipObfuscation
        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return context.scissorStack.peek();
        }

        @SkipObfuscation
        @Override
        public @Nullable ScreenRectangle bounds() {
            ScreenRectangle scissorArea = this.scissorArea();
            return scissorArea == null ? bounds : scissorArea.intersection(bounds);
        }
    }

    public static class Uniform {
        private static final int SIZE = new Std140SizeCalculator()
                .putVec4() // u_Rect
                .putVec4() // u_Radii
                .putVec4() // u_colorRect
                .putVec4() // u_colorRect2
                .putVec4() // u_colorShadow
                .putVec2() // u_gradientDirectionVector
                .putFloat() // u_edgeSoftness
                .putFloat() // u_shadowSoftness
                .get();
        private static final DynamicUniformStorage<DynamicUniformStorage.DynamicUniform> STORAGE =
                new DynamicUniformStorage<>("Ghostify Rounded Rectangle UBO", SIZE, 4);

        public static void clear() {
            STORAGE.endFrame();
        }
    }
}
