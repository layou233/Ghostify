package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.ui.clickgui.fubuki.ScrollWrapper;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class AbstractPage implements Element {
    public static final int LAYER_DEPTH = ClickGUIScreen.LAYER_DEPTH + 2;

    private float startX, startY, endX, endY;
    private final ListView<ModuleItemView> listView;
    private final ScrollWrapper scrollWrapper;

    AbstractPage(Minecraft client, ListView<ModuleItemView> listView) {
        this.listView = listView;
        this.scrollWrapper = new ScrollWrapper(listView);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, long timeDiff) {
        scrollWrapper.render(context, mouseX, mouseY, timeDiff);
    }

    @Override
    public int getLayerDepth() {
        return LAYER_DEPTH;
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
        scrollWrapper.updateStartPosition(startX, startY);
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
        this.endX = newX;
        this.endY = newY;
        scrollWrapper.updateEndPosition(endX, endY);
    }

    @Override
    public void resize() {
        scrollWrapper.resize();
    }

    @Override
    public void remove() {
        scrollWrapper.remove();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return scrollWrapper.shouldCloseOnEsc();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        return scrollWrapper.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        return scrollWrapper.mouseDragged(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        return scrollWrapper.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
