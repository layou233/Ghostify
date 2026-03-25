package com.launium.ghostify.client.ui.font;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

@AllArgsConstructor
public class RenderedText implements AutoCloseable {
    public DynamicTexture texture;
    /**
     * Explain some of its members (for this class):
     * <ul>
     *     <li>width: width of the texture in pixels</li>
     *     <li>height: height of the texture in pixels</li>
     *     <li>x: useless, used internally</li>
     *     <li>y: the y coordinate of text baseline</li>
     * </ul>
     */
    public Rectangle bounds;
    public int lineHeight;
    public TextureSetup textureSetup;

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
        DynamicTexture texture = new DynamicTexture(() -> "Ghostify Smooth Font Texture", nativeImage);
        return new RenderedText(texture, bounds, baseline, TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()));
    }

    public void draw(GuiGraphicsExtractor context, float x, float y, float scale, int color) {
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(x, y);
        pose.scale(1f / scale, 1f / scale, pose);
        // from GuiGraphicsExtractor.innerBlit
        context.guiRenderState.addGuiElement(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED, textureSetup,
                new Matrix3x2f(pose),
                0, 0,
                this.bounds.width, this.bounds.height,
                0f, 1f, 0f, 1f, color,
                context.scissorStack.peek()
        ));
        pose.popMatrix();
    }

    @Override
    public void close() {
        texture.close();
    }
}
