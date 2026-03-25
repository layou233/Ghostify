package com.launium.ghostify.client.ui.clickgui.fubuki.nav;

import com.launium.ghostify.client.ui.RoundRectRenderer;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.clickgui.Element;
import com.launium.ghostify.client.ui.clickgui.fubuki.list.Spinner;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class NavigationSpinner implements Element {
    public float startX, startY;
    public float width, height;
    public float targetRadius = 8F;
    public int layerDepth;
    public final List<? extends NavigationView> destinations;

    private final int color;
    private final Window window;
    private final Animation radius = new Smooth(0F, 8F);
    private final Spinner<? extends NavigationView> spinner;
    private boolean isFocused = true;

    public NavigationSpinner(Window window, int color, int layerDepth, List<? extends NavigationView> destinations) {
        this.destinations = destinations;
        this.color = color;
        this.window = window;
        this.layerDepth = layerDepth;
        this.spinner = new Spinner<>(destinations, 2F, layerDepth + 1);
        this.setFocused(true);
        this.radius.current = this.radius.target;
    }

    public void reset() {
        this.setFocused(true);
        this.spinner.setSelected(0);
        this.radius.current = this.radius.target;
    }

    public void setFocused(boolean focused) {
        this.isFocused = focused;
        this.spinner.setFocused(focused);
    }

    public @Nullable NavigationView getSelected() {
        return this.spinner.getSelected();
    }

    public boolean navigate() {
        NavigationView selected = this.spinner.getSelected();
        if (selected == null) return false;
        return selected.navigate();
    }

    public void spinUp() {
        spinner.spinUp();
    }

    public void spinPageUp() {
        spinner.spinPageUp();
    }

    public void spinDown() {
        spinner.spinDown();
    }

    public void spinPageDown() {
        spinner.spinPageDown();
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, long timeDiff) {
        // tick animations
        if (isFocused) {
            this.radius.target = targetRadius;
        } else {
            this.radius.target = 0F;
        }
        this.radius.tick(timeDiff * 0.05F);

        // render background
        float halfHeight = this.height * 0.5F;
        RoundRectRenderer.State backgroundState = new RoundRectRenderer.State(context,
                this.startX, this.startY - halfHeight, this.startX + this.width, this.startY + halfHeight,
                0F, 1F, color, 0);
        backgroundState.radiusRT = backgroundState.radiusRB = radius.current;
        context.guiRenderState.addPicturesInPictureState(backgroundState);

        // render spinner
        spinner.updateStartPosition(startX, startY - halfHeight);
        spinner.updateEndPosition(startX + width, startY + halfHeight);
        spinner.render(context, mouseX, mouseY, timeDiff);
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
    public void resize() {
        // bad workaround for initialize animation
        float halfHeight = this.height * 0.5F;
        spinner.updateStartPosition(startX, startY - halfHeight);
        spinner.updateEndPosition(startX + width, startY + halfHeight);
        this.spinner.resize();
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        return spinner.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }
}
