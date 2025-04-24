package com.launium.ghostify.client.feature.experimentation;

import com.launium.ghostify.client.GhostifyClient;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.Container;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;

public class UltraSequencerSolver extends AbstractExperimentSolver {
    public static final UltraSequencerSolver INSTANCE = new UltraSequencerSolver();
    public static final String TITLE_PREFIX = "Ultrasequencer (";

    private ArrayList<IntIntImmutablePair> memory = new ArrayList<>(28); // max 28 clicks, number to slot
    private int current = 0;

    @Override
    public boolean willRedirectClick() {
        return state == ExperimentState.SOLVE && !memory.isEmpty();
    }

    @Override
    public int redirectedSlot() {
        int slot = memory.get(current++).rightInt();
        if (current == memory.size()) memory.clear();
        return slot;
    }

    @Override
    public void afterTick(Screen screen) {
        if (screen instanceof ContainerScreen containerScreen) {
            Container container = containerScreen.getMenu().getContainer();
            String stage = container.getItem(49).getHoverName().getString();
            if (state != ExperimentState.REMEMBER && stage.equals("Remember the pattern!")) {
                memory.clear();
                current = 0;
                for (int i = 9; i <= 44; i++) {
                    String itemName = container.getItem(i).getHoverName().getString();
                    if (StringUtils.isNumeric(itemName)) {
                        int number = Integer.parseInt(itemName);
                        memory.add(IntIntImmutablePair.of(number, i));
                    }
                }
                memory.sort(Comparator.comparingInt(IntIntImmutablePair::leftInt));
                state = ExperimentState.REMEMBER;
                GhostifyClient.LOGGER.debug("Recognized Ultrasequencer sequence: {}", memory);
            } else if (stage.startsWith("Timer: ")) {
                if (state == ExperimentState.REMEMBER) startSolvingTimestamp = Util.getMillis();
                state = ExperimentState.SOLVE;
            }
        }
    }

    @Override
    public void onRemove(Screen screen) {
        AbstractExperimentSolver.ACTIVE_SOLVER = null;
        memory.clear();
        state = ExperimentState.INITIALIZE;
        GhostifyClient.LOGGER.info("Ultrasequencer stopped.");
    }
}
