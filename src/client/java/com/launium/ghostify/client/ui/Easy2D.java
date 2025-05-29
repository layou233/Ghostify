package com.launium.ghostify.client.ui;

import com.google.common.collect.ImmutableList;
import com.launium.ghostify.client.mixin.AccessFont;
import com.launium.ghostify.client.mixin.AccessGuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.TriState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class Easy2D {
    private static GuiGraphics context;

    public static void configure(GuiGraphics newContext) {
        context = newContext;
    }

    public static void cleanup() {
        context = null;
    }

    private static final ShaderProgram SHADER_ROUND_RECT = new ShaderProgram(
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
            ROUND_RECT = new RenderType("core/round_rect", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS, 1536, false, false,
            () -> ROUND_RECT_STATES.forEach(RenderStateShard::setupRenderState),
            () -> ROUND_RECT_STATES.forEach(RenderStateShard::clearRenderState)) {
    };

    public static RenderType createTextureRenderType(DynamicTexture texture) {
        return RenderType.create("ghostify_dynamic_texture",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                786432,
                RenderType.CompositeState.builder()
                        .setTextureState(new DynamicTextureStateShard(texture, TriState.FALSE, false))
                        .setShaderState(POSITION_TEXTURE_COLOR_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .createCompositeState(false)
        );
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
        CompiledShaderProgram shader = RenderSystem.setShader(SHADER_ROUND_RECT);
        if (shader == null) {
            return;
        }
        float centerX = (left + right) * 0.5f;
        float centerY = (top + bottom) * 0.5f;
        float extentX = right - left;
        float extentY = bottom - top;
        radius = Math.min(radius, Math.min(extentX, extentY) * 0.5F);
        final float outset = 14F; // conservative
        boolean isPureTranslation = (pose.properties() & Matrix4f.PROPERTY_TRANSLATION) != 0;
        if (isPureTranslation) {
            // fast path
            shader.safeGetUniform("u_Rect")
                    .set(centerX + pose.m30(), centerY + pose.m31(), extentX, extentY);
        } else {
            // here we modify global model view, so cannot do batch rendering
            context.flush();

            // we expect local coordinates, concat pose with model view
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().mul(pose);
            shader.safeGetUniform("u_Rect")
                    .set(centerX, centerY, extentX, extentY);
        }
        shader.safeGetUniform("u_Radii")
                .set(radius, radius, radius, radius);
        Vector4f colorVector = new Vector4f((float) ARGB.red(color) / 255F, (float) ARGB.green(color) / 255F, (float) ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
        shader.safeGetUniform("u_colorRect")
                .set(colorVector);
        shader.safeGetUniform("u_colorRect2")
                .set(colorVector);
        shader.safeGetUniform("u_colorShadow")
                .set(0F, 0F, 0F, ARGB.alpha(color) / 255F);
        shader.safeGetUniform("u_edgeSoftness")
                .set(1F);
        shader.safeGetUniform("u_shadowSoftness")
                .set(shadow);
        var buffer = ((AccessGuiGraphics) context).getBufferSource().getBuffer(ROUND_RECT);
        buffer.addVertex(pose, left - outset, top - outset, depth);
        buffer.addVertex(pose, left - outset, bottom + outset, depth);
        buffer.addVertex(pose, right + outset, bottom + outset, depth);
        buffer.addVertex(pose, right + outset, top - outset, depth);

        // we modify uniform for each draw, so cannot do batch rendering
        context.flush();
        if (!isPureTranslation)
            RenderSystem.getModelViewStack().popMatrix();
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
