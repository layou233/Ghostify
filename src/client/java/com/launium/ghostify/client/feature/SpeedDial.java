package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.config.ContactsBook;
import com.launium.ghostify.client.ui.clickgui.fubuki.nav.NavigationSpinner;
import com.launium.ghostify.client.ui.speeddial.ContactDestinationView;
import com.launium.ghostify.client.util.PlayerHead;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class SpeedDial implements ClientTickEvents.StartTick, ScreenEvents.BeforeInit, ScreenEvents.Remove {
    public static final SpeedDial INSTANCE = new SpeedDial();

    private static final KeyMapping SPEED_DIAL_UP_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.speed_dial_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, GhostifyClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_PAGE_UP_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.speed_dial_page_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, GhostifyClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_DOWN_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.speed_dial_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, GhostifyClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_PAGE_DOWN_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.speed_dial_page_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, GhostifyClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_LEFT_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.speed_dial_left", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, GhostifyClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_RIGHT_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.speed_dial_right", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, GhostifyClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_STAR = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.speed_dial_star", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_ENTER, GhostifyClient.KEY_CATEGORY)
    );

    private NavigationSpinner focusedNav;
    private long lastActionTime = 0L;

    @SkipObfuscation
    @Override
    public void beforeInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof ContainerScreen containerScreen) {
            String title = containerScreen.getTitle().getString();
            if (title.startsWith("Abiphone X") || title.startsWith("Abiphone Flip")) {
                ScreenEvents.remove(screen).register(this);
            }
        }
    }

    @SkipObfuscation
    @Override
    public void onRemove(Screen screen) {
        if (screen instanceof ContainerScreen containerScreen) {
            // scan contacts
            Container container = containerScreen.getMenu().getContainer();
            for (int row = 1; row < 5; row++) {
                for (int column = 1; column < 8; column++) {
                    ItemStack itemStack = container.getItem(row * 9 + column);
                    if (!itemStack.is(Items.PLAYER_HEAD)) {
                        continue;
                    }
                    String itemName = itemStack.getHoverName().getString();
                    if (itemName.startsWith("Abiphone ")) { // probably the shop page
                        return;
                    }
                    String skinB64 = PlayerHead.getSkinFromHead(itemStack).orElse(null);
                    ContactsBook.Contact contact = ConfigManager.CONTACTS.CONTACTS.get(itemName);
                    if (contact == null) { // not found
                        ConfigManager.CONTACTS.CONTACTS.put(itemName, new ContactsBook.Contact(skinB64, false));
                        ConfigManager.CONTACTS.markAsChanged();
                    } else if (!Objects.equals(contact.skin, skinB64)) {
                        ConfigManager.CONTACTS.CONTACTS.put(itemName, new ContactsBook.Contact(skinB64, contact.starred));
                        ConfigManager.CONTACTS.markAsChanged();
                    }
                }
            }
            ConfigManager.processChanges();
        }
    }

    public enum Category {
        STARRED("Starred"), CONTACTS("Contacts");

        public final String name;

        Category(String name) {
            this.name = name;
        }
    }

    public void showCategory(Category category) {
        Window window = Minecraft.getInstance().getWindow();
        switch (category) {
            case STARRED -> {
                GhostifyClient.speedDial.showNavContact(ConfigManager.CONTACTS.CONTACTS
                        .entrySet().stream()
                        .filter(it -> it.getValue().starred)
                        .map(it -> new ContactDestinationView(it.getValue().skin, it.getKey(), window, 3))
                        .toList());
            }
            case CONTACTS -> {
                GhostifyClient.speedDial.showNavContact(ConfigManager.CONTACTS.CONTACTS
                        .entrySet().stream()
                        .map(it -> new ContactDestinationView(it.getValue().skin, it.getKey(), window, 3))
                        .toList()
                );
            }
        }
    }

    @Override
    public void onStartTick(Minecraft client) {
        boolean action = false;
        boolean hideAll = false;
        long now = Util.getMillis();
        if (focusedNav != null && now - lastActionTime > 10_000L) {
            // auto hide if no operation for 10s
            hideAll = true;
        }

        while (SPEED_DIAL_RIGHT_KEY.consumeClick()) {
            if (focusedNav == null) {
                focusedNav = GhostifyClient.speedDial.showNavDock();
            } else if (focusedNav == GhostifyClient.speedDial.navDock) {
                if (focusedNav.navigate()) {
                    focusedNav.setFocused(false);
                    focusedNav = GhostifyClient.speedDial.navContact;
                }
            } else if (focusedNav == GhostifyClient.speedDial.navContact) {
                if (focusedNav.navigate()) {
                    hideAll = true;
                }
            } else {
                throw new IllegalStateException("Unexpected speed dial menu state: " + focusedNav);
            }
            focusedNav.setFocused(true);
            action = true;
        }
        while (SPEED_DIAL_LEFT_KEY.consumeClick() || hideAll) {
            if (focusedNav == null) break;
            //focusedNav.setFocused(false);
            if (focusedNav == GhostifyClient.speedDial.navDock) {
                GhostifyClient.speedDial.hideNavDock();
                focusedNav = null;
            } else if (focusedNav == GhostifyClient.speedDial.navContact) {
                GhostifyClient.speedDial.hideNavContact();
                focusedNav = GhostifyClient.speedDial.navDock;
                focusedNav.setFocused(true);
            }
            action = true;
        }
        while (SPEED_DIAL_UP_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinUp();
            action = true;
        }
        while (SPEED_DIAL_PAGE_UP_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinPageUp();
            action = true;
        }
        while (SPEED_DIAL_DOWN_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinDown();
            action = true;
        }
        while (SPEED_DIAL_PAGE_DOWN_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinPageDown();
            action = true;
        }
        while (SPEED_DIAL_STAR.consumeClick()) {
            if (focusedNav != null && focusedNav == GhostifyClient.speedDial.navContact) {
                ContactDestinationView contactDestinationView = (ContactDestinationView) focusedNav.getSelected();
                if (contactDestinationView != null) {
                    ContactsBook.Contact contact = ConfigManager.CONTACTS.CONTACTS.get(contactDestinationView.contactName);
                    if (contact != null) { // I don't know how to handle it if it's null just why?
                        ConfigManager.CONTACTS.CONTACTS.put(contactDestinationView.contactName,
                                new ContactsBook.Contact(contact.skin, !contact.starred));
                        ConfigManager.CONTACTS.markAsChanged();
                        ConfigManager.processChanges();
                        if (client.player != null) {
                            client.player.displayClientMessage(Component.literal(
                                            "[Ghostify] Marked " + contactDestinationView.contactName + " as " + (contact.starred ? "not starred." : "starred.")),
                                    false);
                        }
                    }
                }
            }
        }

        if (action) {
            lastActionTime = now;
        }
    }
}
