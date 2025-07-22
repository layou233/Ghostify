package com.launium.ghostify.client.ui.clickgui.fubuki;

import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.clickgui.Element;
import net.minecraft.client.gui.GuiGraphics;

public class FragmentView implements Element {
    public Element currentFrag;
    public Element previousFrag;
    public float startX, startY, endX, endY;
    public int layerDepth;

    private final Animation transitionProgress = new Smooth(0F, 0F);

    public FragmentView(Element initialFrag, int layerDepth) {
        this.currentFrag = initialFrag;
        this.layerDepth = layerDepth;
    }

    public void push(Element next) {
        if (next == this.currentFrag) return;
        this.previousFrag = this.currentFrag;
        this.currentFrag = next;
        this.transitionProgress.current = 1F;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        // tick animations
        transitionProgress.tick(timeDiff * 0.008F);

        float offset = 0F;
        if (previousFrag != null) offset = (endX - startX) * transitionProgress.current;

        context.enableScissor((int) Math.floor(startX), (int) Math.floor(startY),
                (int) Math.ceil(endX), (int) Math.ceil(endY));

        if (previousFrag != null) {
            float width = endX - startX;
            previousFrag.updateStartPosition(startX + (width - offset), startY);
            previousFrag.updateEndPosition(endX + (width - offset), endY);
            previousFrag.render(context, mouseX, mouseY, timeDiff);
            if (transitionProgress.current < 0.0005F) {
                previousFrag.remove();
                previousFrag = null;
            }
        }

        currentFrag.updateStartPosition(startX - offset, startY);
        currentFrag.updateEndPosition(endX - offset, endY);
        currentFrag.render(context, mouseX, mouseY, timeDiff);

        context.disableScissor();
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
        this.endX = newX;
        this.endY = newY;
    }

    @Override
    public void resize() {
        if (currentFrag != null) currentFrag.resize();
    }

    @Override
    public void remove() {
        if (previousFrag != null) previousFrag.remove();
        if (currentFrag != null) currentFrag.remove();
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return currentFrag == null || currentFrag.shouldCloseOnEsc();
    }

    private boolean isInBounds(float x, float y) {
        return startX < x && x < endX && startY < y && y < endY;
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        return isInBounds(mouseX, mouseY) && currentFrag != null && currentFrag.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        return isInBounds(mouseX, mouseY) && currentFrag != null && currentFrag.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        return isInBounds(mouseX, mouseY) && currentFrag != null && currentFrag.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
