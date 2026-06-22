package com.launium.ghostify.client.ui;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.EqualsAndHashCode;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public class RoundRectRenderer extends PictureInPictureRenderer<RoundRectRenderer.State> {
    private State lastState;

    public static void init() {
        PictureInPictureRendererRegistry.register(context -> new RoundRectRenderer());
    }

    @Override
    public @NonNull Class<State> getRenderStateClass() {
        return State.class;
    }

    @Override
    protected boolean textureIsReadyToBlit(State state) {
        return Objects.equals(lastState, state);
    }

    @Override
    protected void renderToTexture(State state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector) {
        float width = (state.extentX + 2 * State.OUTSET) * state.scale;
        float height = (state.extentY + 2 * State.OUTSET) * state.scale;
        float textureWidth = width + state.subpixelX * state.scale;
        float textureHeight = height + state.subpixelY * state.scale;

        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION.getVertexSize() * 4);
        BufferBuilder builder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
        builder.addVertex(0F, 0F, 0F);
        builder.addVertex(0F, textureHeight, 0F);
        builder.addVertex(textureWidth, textureHeight, 0F);
        builder.addVertex(textureWidth, 0F, 0F);
        MeshData mesh = builder.buildOrThrow();

        GpuBufferSlice dynamicTransformsBuffer = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                new Vector4f(),
                new Vector3f(),
                new Matrix4f()
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
        GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "Ghostify Rounded Rectangle Vertices", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
        RenderSystem.AutoStorageIndexBuffer indexStorage = RenderSystem.getSequentialBuffer(mesh.drawState().primitiveTopology());
        GpuBuffer indexBuffer = indexStorage.getBuffer(mesh.drawState().indexCount());
        // During PIP renderToTexture the output overrides point at this renderer's off-screen
        // color/depth texture (set by PictureInPictureRenderer#prepare), so render straight into them.
        try (
                byteBufferBuilder;
                mesh;
                vertexBuffer;
                RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "Ghostify Rounded Rectangle Render Pass",
                        RenderSystem.outputColorTextureOverride,
                        Optional.empty(),
                        RenderSystem.outputDepthTextureOverride,
                        OptionalDouble.empty()
                )
        ) {
            pass.setPipeline(GhostifyRenderTypes.PIPELINE_ROUND_RECT);
            // store scissor areas in render states may reduce texture reuse, so the render scissor is temporarily not applied
            // scissoring effect would be still applied at the screen blit stage
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransformsBuffer);
            pass.setUniform("u", myUniformBuffer);
            pass.setVertexBuffer(0, vertexBuffer.slice());
            pass.setIndexBuffer(indexBuffer, indexStorage.type());
            pass.drawIndexed(mesh.drawState().indexCount(), 1, 0, 0, 0);
        }

        lastState = state;
    }

    @Override
    protected @NonNull String getTextureLabel() {
        return "Ghostify Rounded Rectangle PIP";
    }

    @EqualsAndHashCode
    public static class State implements PictureInPictureRenderState {
        public final static float OUTSET = 14F; // conservative

        public transient final float left, top, right, bottom;
        public final float scale;
        public final float shadow;
        public final Vector4f color, shadowColor; // normalized RGBA
        public @Nullable Vector4f color2;
        public float radiusRB, radiusRT, radiusLB, radiusLT; // left-right; top-bottom
        public float gradiantDirectionX = 0F, gradiantDirectionY = 0F;
        public float edgeSoftness = 1F;

        private final float subpixelX, subpixelY; // pad texture for subpixel rendering
        private final float extentX, extentY;
        private transient final ScreenRectangle scissorArea;
        private transient final ScreenRectangle bounds;

        public State(GuiGraphicsExtractor context, float left, float top, float right, float bottom,
                     float radius, float shadow, int color, int shadowColor) {
            if (!Float.isFinite(radius) || radius < 0.0f) { // NaN, Inf, negative
                radius = 0;
            }

            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.subpixelX = left - Mth.floor(left);
            this.subpixelY = top - Mth.floor(top);
            this.scale = Minecraft.getInstance().getWindow().getGuiScale();
            this.shadow = shadow;
            this.color = new Vector4f(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
            this.shadowColor = new Vector4f(ARGB.red(shadowColor) / 255F, ARGB.green(shadowColor) / 255F, ARGB.blue(shadowColor) / 255F, ARGB.alpha(shadowColor) / 255F);

            this.extentX = right - left;
            this.extentY = bottom - top;
            this.radiusLT = this.radiusRT = this.radiusLB = this.radiusRB = Math.min(radius, Math.min(extentX, extentY) * 0.5F);
            this.scissorArea = context.scissorStack.peek();
            ScreenRectangle bounds = new ScreenRectangle(this.x0(), this.y0(), this.x1() - this.x0(), this.y1() - this.y0());
            this.bounds = scissorArea == null ? bounds : scissorArea.intersection(bounds);
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


        @Override
        public int x0() {
            return Mth.floor(left) - (int) OUTSET;
        }


        @Override
        public int x1() {
            return Mth.ceil(right + OUTSET);
        }


        @Override
        public int y0() {
            return Mth.floor(top) - (int) OUTSET;
        }


        @Override
        public int y1() {
            return Mth.ceil(bottom + OUTSET);
        }


        @Override
        public float scale() {
            return 1F;
        }


        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return scissorArea;
        }


        @Override
        public @Nullable ScreenRectangle bounds() {
            return bounds;
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
