package com.launium.ghostify.client.ui.speeddial;

import com.launium.ghostify.client.feature.SpeedDial;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.clickgui.fubuki.nav.NavigationView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

class DockDestinationView implements NavigationView {
    private final ItemStack itemIcon;
    private final SpeedDial.Category category;
    private final int layerDepth;

    private float startX, startY;

    DockDestinationView(ItemStack icon, SpeedDial.Category category, int layerDepth) {
        this.itemIcon = icon;
        this.category = category;
        this.layerDepth = layerDepth;
    }

    @Override
    public boolean navigate() {
        SpeedDial.INSTANCE.showCategory(category);
        return true;
    }

    @Override
    public float measureHeight() {
        return 16F;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        //context.fill((int) startX, (int) startY, (int) startX + 30, (int) startY + (int) measureHeight(), 0xAFFFFFFF);
        Easy2D.drawItem(itemIcon, startX + 2F, startY);
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
    public int getLayerDepth() {
        return layerDepth;
    }

    @Override
    public String name() {
        return category.name;
    }
}
