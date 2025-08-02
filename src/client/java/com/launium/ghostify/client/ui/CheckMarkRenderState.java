package com.launium.ghostify.client.ui;

import com.launium.ghostify.client.ui.animation.Animation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public class CheckMarkRenderState implements GuiElementRenderState {
    public static final float LENGTH = 60F;

    private static final float STARTING_PERCENT = 0F;
    private static final float INFLECTION_PERCENT = 0.6F;

    private final float x, y, scale;
    private final int color;
    private final GuiGraphics context;
    private final Animation progress;
    private final ScreenRectangle bounds;

    public CheckMarkRenderState(GuiGraphics context, float x, float y, float scale, int color, Animation progress) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.color = color;
        this.context = context;
        this.progress = progress;

        this.bounds = new ScreenRectangle((int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(LENGTH * scale), (int) Math.ceil(LENGTH * scale));
    }

    @Override
    public void buildVertices(VertexConsumer consumer, float z) {
        float progressRatio = this.progress.ratio();
        if (progressRatio < 0F) progressRatio = 0F;
        else if (progressRatio > 1F) progressRatio = 1F;

        Matrix3x2f pose = context.pose();
        if (progressRatio > STARTING_PERCENT) {
            float ratio = (progressRatio - STARTING_PERCENT) / (INFLECTION_PERCENT - STARTING_PERCENT);
            if (ratio > 1F) ratio = 1F;
            consumer.addVertexWith2DPose(pose, this.x + this.scale * 15F, this.y + this.scale * 32F, z).setColor(this.color);
            consumer.addVertexWith2DPose(pose, this.x + this.scale * 18F, this.y + this.scale * 29F, z).setColor(this.color);
            consumer.addVertexWith2DPose(pose, this.x + this.scale * (18F + (8F * ratio)), this.y + this.scale * (29F + (8F * ratio)), z).setColor(this.color); // (26, 37)
            consumer.addVertexWith2DPose(pose, this.x + this.scale * (15F + (11F * ratio)), this.y + this.scale * (32F + (11F * ratio)), z).setColor(this.color); // (26, 43)

            if (progressRatio > INFLECTION_PERCENT) {
                ratio = (progressRatio - INFLECTION_PERCENT) / (1F - INFLECTION_PERCENT);
                if (ratio > 1F) ratio = 1F;
                consumer.addVertexWith2DPose(pose, this.x + this.scale * 26F, this.y + this.scale * 37F, z).setColor(this.color); // (26, 37)
                consumer.addVertexWith2DPose(pose, this.x + this.scale * 26F, this.y + this.scale * 43F, z).setColor(this.color); // (26, 43)
                consumer.addVertexWith2DPose(pose, this.x + this.scale * (26F + (19F * ratio)), this.y + this.scale * (43F - (19F * ratio)), z).setColor(this.color); // (45, 24)
                consumer.addVertexWith2DPose(pose, this.x + this.scale * (26F + (16F * ratio)), this.y + this.scale * (37F - (16F * ratio)), z).setColor(this.color); // (42, 21)
            }
        }
    }

    @Override
    public @NotNull RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public @NotNull TextureSetup textureSetup() {
        return TextureSetup.noTexture();
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
}
