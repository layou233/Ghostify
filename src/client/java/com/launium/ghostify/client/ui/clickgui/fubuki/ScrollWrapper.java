package com.launium.ghostify.client.ui.clickgui.fubuki;

import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.clickgui.Element;
import net.minecraft.client.gui.GuiGraphics;

public class ScrollWrapper implements Element, CullingProvider {
    public Element child;
    private float startX, startY, endX, endY;
    private final Animation verticalScroll = new Smooth(0F, 0F);

    public ScrollWrapper(Element child) {
        this.child = child;
        if (child instanceof CullingReceiver receiver) {
            receiver.setCullingProvider(this);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        // tick animations
        verticalScroll.tick(timeDiff * 0.05F);

        child.updateStartPosition(startX, startY + verticalScroll.current);
        child.updateEndPosition(endX, endY + verticalScroll.current);

        context.enableScissor((int) Math.floor(startX), (int) Math.floor(startY),
                (int) Math.ceil(endX), (int) Math.ceil(endY));
        child.render(context, mouseX, mouseY, timeDiff);
        context.disableScissor();
    }

    @Override
    public boolean canBeCulled(float startX, float startY, float endX, float endY) {
        return startX > this.endX || startY > this.endY
                || endX < this.startX || endY < this.startY;
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
        child.resize();
    }

    @Override
    public void remove() {
        child.remove();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return child.shouldCloseOnEsc();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        return child.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        return child.mouseDragged(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        if (!child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            verticalScroll.target += scrollY*3;
        }
        return true;
    }

    @Override
    public int getLayerDepth() {
        return child.getLayerDepth();
    }
}
