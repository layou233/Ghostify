package com.launium.ghostify.client.ui.font;

import com.launium.ghostify.client.mixin.AccessGuiGraphics;
import com.launium.ghostify.client.ui.GhostifyRenderTypes;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

@AllArgsConstructor
public class RenderedText implements AutoCloseable {
    public DynamicTexture texture;
    public Rectangle bounds;
    public int lineHeight;
    public RenderType renderType;

    private static final Color COLOR_TRANSPARENT = new Color(0, true);

    public static RenderedText create(RenderInfo info) {
        Font font = info.font().deriveFont(info.size());
        GlyphVector glyphVector = font.createGlyphVector(FontManager.FONT_RENDER_CONTEXT, info.text());
        Rectangle bounds = glyphVector.getPixelBounds(null, 0, 0);
        int baseline = -bounds.y;
        BufferedImage bufferedImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = bufferedImage.createGraphics();
        imageGraphics.setFont(font);
        imageGraphics.setPaint(COLOR_TRANSPARENT);
        imageGraphics.setComposite(AlphaComposite.Clear);
        imageGraphics.fillRect(0, 0, bounds.width, bounds.height);
        imageGraphics.setPaint(Color.WHITE);
        imageGraphics.setComposite(AlphaComposite.SrcOver);
        imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        imageGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        imageGraphics.drawString(info.text(), -bounds.x, baseline);
        imageGraphics.dispose();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        // Fuck Minecraft native image
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                nativeImage.setPixel(x, y, bufferedImage.getRGB(x, y));
            }
        }
        DynamicTexture texture = new DynamicTexture(nativeImage);
        return new RenderedText(texture, bounds, baseline, GhostifyRenderTypes.createTextureRenderType(texture));
    }

    public void draw(GuiGraphics context, float x, float y, float z, float scale, int color) {
        context.pose().pushPose();
        context.pose().scale(1f / scale, 1f / scale, 1f);
        Matrix4f pose = context.pose().last().pose();
        // from GuiGraphics.innerBlit
        MultiBufferSource.BufferSource bufferSource = ((AccessGuiGraphics) context).getBufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(this.renderType);
        buffer.addVertex(pose, x * scale, y * scale, z).setUv(0f, 0f).setColor(color);
        buffer.addVertex(pose, x * scale, y * scale + this.bounds.height, z).setUv(0f, 1f).setColor(color);
        buffer.addVertex(pose, x * scale + this.bounds.width, y * scale + this.bounds.height, z).setUv(1f, 1f).setColor(color);
        buffer.addVertex(pose, x * scale + this.bounds.width, y * scale, z).setUv(1f, 0f).setColor(color);
        context.pose().popPose();
    }

    @Override
    public void close() {
        texture.close();
    }
}
