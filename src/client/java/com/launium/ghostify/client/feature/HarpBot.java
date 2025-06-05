package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.mixin.AccessAbstractContainerScreen;
import com.launium.ghostify.client.util.ClientTaskScheduler;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Objects;

public class HarpBot extends AbstractModule implements ClientTickEvents.StartTick, ScreenEvents.AfterTick, ScreenEvents.Remove {
    public static final HarpBot INSTANCE = new HarpBot();
    public static float delayMultiplierMin = 0.6F;
    public static float delayMultiplierMax = 0.63F;
    private static final KeyMapping HARP_BOT_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.ghostify.switch_harp_bot", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), GhostifyClient.KEY_CATEGORY)
    );

    private final RandomSource random = RandomSource.create();
    private final String[] currentChart = new String[35];
    private long lastChangeTimestamp = 0;
    private boolean isEnabled = false;
    private boolean isActive = false;

    public void init() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (isEnabled && screen instanceof ContainerScreen) {
                if (!screen.getTitle().getString().startsWith("Harp - ")) return;
                Arrays.fill(currentChart, null);
                isActive = true;
                ScreenEvents.afterTick(screen).register(this);
                ScreenEvents.remove(screen).register(this);
                GhostifyClient.LOGGER.info("Harp started.");
            }
        });
    }

    @Override
    public void onStartTick(Minecraft client) {
        while (HARP_BOT_KEY.consumeClick()) {
            isEnabled = !isEnabled;
            if (isEnabled) GhostifyClient.moduleList.showModule(this);
        }
    }

    @Override
    public void afterTick(Screen screen) {
        if (screen instanceof ContainerScreen containerScreen) {
            Container container = containerScreen.getMenu().getContainer();
            boolean isChanged = false;
            for (int i = 1; i <= 34; i++) { // scan full chart to identify changes
                String itemDescriptor = container.getItem(i).getItem().getDescriptionId();
                if (!isChanged && !Objects.equals(itemDescriptor, currentChart[i])) {
                    isChanged = true;
                }
                currentChart[i] = itemDescriptor;
            }
            if (isChanged) {
                for (int i = 28; i <= 34; i++) { // scan the last row
                    if (currentChart[i].endsWith("_wool")) {
                        long expectedTime = Util.getMillis();
                        long diff;
                        if ((diff = expectedTime - lastChangeTimestamp) < 5000L) { // in 5 seconds
                            // adapt to flow speed
                            expectedTime += random.nextIntBetweenInclusive((int) (delayMultiplierMin * diff), (int) (delayMultiplierMax * diff));
                        } else { // should be unreachable
                            expectedTime += random.nextIntBetweenInclusive(120, 160);
                        }
                        int slotToClick = i + 9; // next row, the keys
                        ClientTaskScheduler.CLIENT_TASKS.add(new ClientTaskScheduler.AbstractTask(expectedTime) {
                            @Override
                            public void execute(Minecraft client) {
                                if (INSTANCE.isActive) {
                                    //GhostifyClient.LOGGER.info("Clicking on {}", slotToClick);
                                    ((AccessAbstractContainerScreen) containerScreen).slotClicked(
                                            containerScreen.getMenu().getSlot(slotToClick), slotToClick,
                                            GLFW.GLFW_MOUSE_BUTTON_3, ClickType.CLONE // use middle-click
                                    );
                                }
                            }
                        });
                    }
                }
                lastChangeTimestamp = Util.getMillis();
            }
        }
    }

    @Override
    public void onRemove(Screen screen) {
        isActive = false;
        Arrays.fill(currentChart, null);
        GhostifyClient.LOGGER.info("Harp stopped.");
    }

    @Override
    public String title() {
        return "HarpBot";
    }

    @Override
    public @Nullable String subtitle() {
        return null;
    }

    @Override
    public boolean isActive() {
        return isEnabled;
    }
}
