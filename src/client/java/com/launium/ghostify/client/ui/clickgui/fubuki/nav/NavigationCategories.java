package com.launium.ghostify.client.ui.clickgui.fubuki.nav;

import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.clickgui.Element;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.font.RenderInfo;
import com.launium.ghostify.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class NavigationCategories implements Element {
    public float startX, startY;
    public float width, height;
    public float buttonHeight;
    public float fontSize;
    public final ArrayList<NavigationDestination> destinations = new ArrayList<>();
    public int layerDepth;

    private final Window window;
    private int selectedIndex = 0;
    private Animation highlightStartX = new Smooth(0, 0);
    private Animation highlightEndX = new Smooth(0, 0);
    private List<FloatFloatImmutablePair> categoryTextBounds;

    public NavigationCategories(float fontSize, Window window, int layerDepth) {
        this.buttonHeight = height;
        this.fontSize = fontSize;
        this.window = window;
        this.layerDepth = layerDepth;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();

        // tick animations
        highlightStartX.tick(timeDiff * 0.02F);
        highlightEndX.tick(timeDiff * 0.02F);

        // draw selected background
        final float buttonY = startY + (height - buttonHeight) * 0.5F;
        Easy2D.drawRoundRect(highlightStartX.current + startX, buttonY,
                highlightEndX.current + startX, buttonY + buttonHeight, this.getLayerDepth(), buttonHeight * 0.5F,
                16F, 0xFF004CFF, 0xFF004CFF);

        // draw navigation buttons
        if (destinations.isEmpty()) { // should be avoided
            highlightStartX.target = startX;
            highlightEndX.target = startX + width;
        } else {
            List<RenderedText> categoryRenderedTexts = destinations.stream()
                    .map(destination -> {
                        String name = destination.name();
                        if (name == null || name.isBlank()) name = "|EMPTY|"; // wow
                        return FontManager.requestRenderedText(
                                new RenderInfo(FontManager.DEFAULT_FONT, name, fontSize), (float) window.getGuiScale()
                        );
                    }).toList();
            float gapWidth = destinations.size() == 1 ? 0F : (float) ((width - categoryRenderedTexts.stream()
                    .mapToDouble(it -> it.bounds.width / scale)
                    .reduce(Double::sum)
                    .orElseThrow()) / (destinations.size() - 1));
            float currentX = startX;
            ArrayList<FloatFloatImmutablePair> bounds = new ArrayList<>(categoryRenderedTexts.size());
            for (int i = 0; i < categoryRenderedTexts.size(); i++) {
                RenderedText text = categoryRenderedTexts.get(i);
                // TODO: pass alpha value
                text.draw(context, currentX, startY + (height + text.bounds.y / scale) * 0.5F, 1F, (float) window.getGuiScale(), 0xFFFFFFFF);
                if (i == selectedIndex) {
                    highlightStartX.target = currentX - buttonHeight * 0.5F - startX;
                    highlightEndX.target = currentX + text.bounds.width / scale + buttonHeight * 0.5F - startX;
                }
                bounds.add(new FloatFloatImmutablePair(currentX, currentX += text.bounds.width / scale));
                currentX += gapWidth;
            }
            categoryTextBounds = bounds;
        }
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (startX <= mouseX && mouseX <= startX + width
                && startY <= mouseY && mouseY <= startY + height) {
            List<FloatFloatImmutablePair> bounds = categoryTextBounds;
            for (int i = 0; i < bounds.size(); i++) {
                FloatFloatImmutablePair bound = categoryTextBounds.get(i);
                if (bound.leftFloat() <= mouseX && mouseX <= bound.rightFloat()) {
                    if (selectedIndex != i && destinations.get(i).navigate()) {
                        selectedIndex = i;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void resize() {
        // avoid unwanted animations caused by window size changes
        highlightStartX.current = highlightStartX.target;
        highlightEndX.current = highlightEndX.target;
    }

    @Override
    public void remove() {
        this.categoryTextBounds.clear();
        this.categoryTextBounds = null;
        Element.super.remove();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return Element.super.shouldCloseOnEsc();
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }
}
