package com.launium.ghostify.client.ui.clickgui;

import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.Space;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.clickgui.fubuki.FragmentView;
import com.launium.ghostify.client.ui.clickgui.fubuki.nav.DummyNavigationDestination;
import com.launium.ghostify.client.ui.clickgui.fubuki.nav.NavigationCategories;
import com.launium.ghostify.client.ui.clickgui.fubuki.nav.NavigationDestination;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.font.RenderInfo;
import com.launium.ghostify.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;

import java.util.PriorityQueue;

public class ClickGUIScreen extends Screen {
    public static final int LAYER_DEPTH = 1;

    private final Screen parent;
    private final Window window;
    private final PriorityQueue<Element> children = new PriorityQueue<>();
    private final NavigationCategories nav;
    private final FragmentView listFragment;

    private static final float HALF_WIDTH = 180F;
    private static final float HALF_HEIGHT = 110F;
    private static final float OUTER_RADIUS = 24F;
    private static final float OUTER_PADDING = 18F;

    private float lastMouseClickRelativeX = 0, lastMouseClickRelativeY = 0;
    private long lastTickTime = 0;
    private final Animation alpha = new Smooth(0F, 0xEF);

    float centerX, centerY;

    public ClickGUIScreen(Minecraft client, Screen parent) {
        super(Component.literal("Ghostify ClickGUI"));
        this.parent = parent;

        this.window = client.getWindow();
        this.centerX = (float) Math.ceil(window.getGuiScaledWidth() * 0.5F);
        this.centerY = (float) Math.ceil(window.getGuiScaledHeight() * 0.5F);

        this.listFragment = new FragmentView(new GeneralPage(client), LAYER_DEPTH + 1);
        this.children.add(this.listFragment);

        this.nav = new NavigationCategories(10F, window, LAYER_DEPTH + 1);
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "General";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new GeneralPage(client));
                return true;
            }
        });
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "Features";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new FeaturesPage(client));
                return true;
            }
        });
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "Patches";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new PatchesPage(client));
                return true;
            }
        });
        this.nav.destinations.add((DummyNavigationDestination) () -> "Commands");
        this.children.add(this.nav);
        //this.children.add(new FragmentView(0F, 0F, new GeneralPage(), LAYER_DEPTH + 1));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        super.render(context, mouseX, mouseY, partialTick);
        Easy2D.configure(context);

        float scale = (float) window.getGuiScale();
        long now = Util.getMillis();
        long timeDiff = now - lastTickTime;
        if (timeDiff > 40) timeDiff = 40;
        Space space = new Space(2 * (HALF_WIDTH - OUTER_PADDING), 2 * (HALF_HEIGHT - OUTER_PADDING));
        space.translate(centerX - HALF_WIDTH + OUTER_PADDING, centerY - HALF_HEIGHT + OUTER_PADDING);

        // tick animations
        alpha.tick(timeDiff * 0.005F);

        float startX = centerX - HALF_WIDTH;
        float startY = centerY - HALF_HEIGHT;
        // draw background
        Easy2D.drawRoundRect(startX, startY, centerX + HALF_WIDTH, centerY + HALF_HEIGHT,
                OUTER_RADIUS, 16F, ARGB.color((int) alpha.current, 0), 0xFFFFFFFF);
        // draw title
        RenderedText titleText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, "GHOSTIFY", 14F), (float) window.getGuiScale());
        titleText.draw(context, startX + OUTER_PADDING, startY + OUTER_PADDING, (float) window.getGuiScale(), ARGB.color((int) alpha.current, 0xFFFFFF));

        // set navigation categories
        this.nav.updateStartPosition(
                startX + OUTER_PADDING + titleText.bounds.width / scale + OUTER_PADDING,
                startY + OUTER_PADDING);
        this.nav.width = HALF_WIDTH * 2 - OUTER_PADDING * 3 - titleText.bounds.width / scale;
        this.nav.height = titleText.bounds.height / scale;
        this.nav.buttonHeight = this.nav.height + 4F;

        // set left list
        this.listFragment.updateStartPosition(startX + OUTER_PADDING, startY + OUTER_PADDING + titleText.bounds.height / scale + OUTER_PADDING);
        this.listFragment.updateEndPosition(centerX + HALF_WIDTH - OUTER_PADDING, centerY + HALF_HEIGHT - OUTER_PADDING);

        // draw children
        for (Element child : children) {
            child.render(context, mouseX, mouseY, timeDiff);
        }

        Easy2D.cleanup();
        lastTickTime = now;
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics context) {
        if (ConfigManager.GENERAL.CLICK_GUI_BLUR) {
            super.renderBlurredBackground(context);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        float mouseXF = (float) event.x();
        float mouseYF = (float) event.y();
        lastMouseClickRelativeX = mouseXF - centerX;
        lastMouseClickRelativeY = mouseYF - centerY;

        for (Element child : children) {
            if (child.mouseClicked(mouseXF, mouseYF)) return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        float mouseXF = (float) event.x();
        float mouseYF = (float) event.y();
        centerX = mouseXF - lastMouseClickRelativeX;
        centerY = mouseYF - lastMouseClickRelativeY;

        for (Element child : children) {
            if (child.mouseDragged(mouseXF, mouseYF, (float) dragX, (float) dragY)) return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Element child : children) {
            if (child.mouseScrolled((float) mouseX, (float) mouseY, (float) scrollX, (float) scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void resize(int width, int height) {
        for (Element child : children) {
            child.resize();
        }
        super.resize(width, height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return children.stream().allMatch(Element::shouldCloseOnEsc);
    }

    @Override
    public void removed() {
        for (Element child : children) {
            child.remove();
        }
        super.removed();
    }

    @Override
    public void onClose() {
        ConfigManager.processChanges();
        this.minecraft.setScreen(parent);
    }
}
