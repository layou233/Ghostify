package com.launium.ghostify.client.ui.clickgui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;

public interface Element extends Comparable<Element> {
    default void render(GuiGraphicsExtractor context, int mouseX, int mouseY, long timeDiff) {
    }

    void updateStartPosition(float newX, float newY);

    void updateEndPosition(float newX, float newY);

    default void resize() {
    }

    default void remove() {
    }

    default boolean shouldCloseOnEsc() {
        return true;
    }

    default boolean mouseClicked(float mouseX, float mouseY) {
        return false;
    }

    default boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        return false;
    }

    default boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        return false;
    }

    int getLayerDepth();

    @Override
    default int compareTo(@NonNull Element o) {
        return Integer.compare(this.getLayerDepth(), o.getLayerDepth());
    }
}
