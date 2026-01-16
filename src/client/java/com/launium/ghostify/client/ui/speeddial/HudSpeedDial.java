package com.launium.ghostify.client.ui.speeddial;

import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.launium.ghostify.client.feature.SpeedDial;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.clickgui.fubuki.nav.NavigationSpinner;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class HudSpeedDial implements HudElement {
    private static final float NAV_DOCK_WIDTH = 20F;
    private static final float NAV_CONTACT_WIDTH = 70F;
    private static final float HEIGHT = 92F;

    public NavigationSpinner navDock;
    public final Animation navDockX = new Smooth(-NAV_DOCK_WIDTH, 0F);

    public NavigationSpinner navContact;
    public final Animation navContactX = new Smooth(-NAV_DOCK_WIDTH - NAV_CONTACT_WIDTH, 0F);

    private long lastRenderTime = 0;

    public NavigationSpinner showNavDock() {
        NavigationSpinner navDock = this.navDock;
        if (navDock == null) {
            navDock = new NavigationSpinner(Minecraft.getInstance().getWindow(),
                    0xDB000000, 1, List.of(
                    new DockDestinationView(new ItemStack(Items.NETHER_STAR), SpeedDial.Category.STARRED, 2),
                    new DockDestinationView(new ItemStack(Items.KNOWLEDGE_BOOK), SpeedDial.Category.CONTACTS, 2)
            ));
            navDock.width = NAV_DOCK_WIDTH;
            navDock.height = HEIGHT;
            navDock.targetRadius = NAV_DOCK_WIDTH * 0.3F;
        }
        navDockX.current = -NAV_DOCK_WIDTH;
        navDockX.target = 0F;
        navDock.resize();
        this.navDock = navDock;
        return navDock;
    }

    public void hideNavDock() {
        this.navDockX.target = -2F - NAV_DOCK_WIDTH;
    }

    public void showNavContact(List<ContactDestinationView> contacts) {
        if (navContact != null) {
            navContact.remove();
        }
        navContact = new NavigationSpinner(Minecraft.getInstance().getWindow(),
                0xAB000000, 1, contacts);
        navContact.width = NAV_CONTACT_WIDTH;
        navContact.height = HEIGHT;
        navContact.targetRadius = NAV_DOCK_WIDTH * 0.3F; // keep the same with dock
        navContactX.current = -NAV_DOCK_WIDTH - NAV_CONTACT_WIDTH;
        navContactX.target = NAV_DOCK_WIDTH;
        navContact.resize();
    }

    public void hideNavContact() {
        this.navContactX.target = -2F - NAV_DOCK_WIDTH - NAV_CONTACT_WIDTH;
    }

    @SkipObfuscation
    @Override
    public void render(GuiGraphics context, DeltaTracker deltaTracker) {
        if (navDock == null) return;

        Easy2D.configure(context);
        long now = Util.getMillis();
        final long timeDiff; // final for lambda
        {
            long _timeDiff = now - lastRenderTime;
            if (_timeDiff > 40) timeDiff = 40;
            else timeDiff = _timeDiff;
        }

        // tick animations
        navDockX.tick(timeDiff * 0.01F);
        navContactX.tick(timeDiff * 0.01F);

        Window window = Minecraft.getInstance().getWindow();
        // render contacts navigation spinner
        if (navContact != null) {
            navContact.updateStartPosition(navContactX.current, window.getGuiScaledHeight() * 0.5F);
            navContact.render(context, 0, 0, timeDiff);
            if (navContactX.target == -2F - NAV_DOCK_WIDTH - NAV_CONTACT_WIDTH && navContactX.ratio() > 0.99F) {
                navContact.remove();
                navContact = null;
            }
        }

        // render dock navigation spinner
        navDock.updateStartPosition(navDockX.current, window.getGuiScaledHeight() * 0.5F);
        navDock.render(context, 0, 0, timeDiff);
        if (navDockX.target == -2F - NAV_DOCK_WIDTH && navDockX.ratio() > 0.99F) {
            navDock.remove();
            navDock = null;
        }

        Easy2D.cleanup();
        lastRenderTime = now;
    }
}
