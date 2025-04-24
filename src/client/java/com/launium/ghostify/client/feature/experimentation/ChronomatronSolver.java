package com.launium.ghostify.client.feature.experimentation;

import com.launium.ghostify.client.GhostifyClient;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.Container;

import java.util.ArrayList;

public class ChronomatronSolver extends AbstractExperimentSolver {
    public static final ChronomatronSolver INSTANCE = new ChronomatronSolver();
    public static final String TITLE_PREFIX = "Chronomatron (";

    private ArrayList<Integer> memory = new ArrayList<>(49 + 1); // max 49 notes, stores slot IDs
    private boolean isShowingGlint = false;
    private int current = 0;

    @Override
    public boolean willRedirectClick() {
        return state == ExperimentState.SOLVE && !memory.isEmpty();
    }

    @Override
    public int redirectedSlot() {
        int slot = memory.get(current++);
        if (current == memory.size()) memory.clear();
        return slot;
    }

    @Override
    public void afterTick(Screen screen) {
        if (screen instanceof ContainerScreen containerScreen) {
            Container container = containerScreen.getMenu().getContainer();
            String stage = container.getItem(49).getHoverName().getString();
            if (stage.equals("Remember the pattern!")) {
                if (state != ExperimentState.REMEMBER) {
                    //GhostifyClient.LOGGER.info("New round");
                    memory.clear();
                    isShowingGlint = false;
                    if (state == ExperimentState.SOLVE) {
                        // skip the first glint after solving
                        current = 1;
                    } else {
                        current = 0;
                    }
                    state = ExperimentState.REMEMBER;
                }
                scanNote(container);
            } else if (stage.startsWith("Timer: ")) {
                if (current <= 1) {
                    // sorry for this shit but
                    // the last note may come with Timer as an edge case
                    scanNote(container);
                }
                if (state == ExperimentState.REMEMBER) startSolvingTimestamp = Util.getMillis();
                state = ExperimentState.SOLVE;
            }
        }
    }

    private void scanNote(Container container) {
        int glintIndex = -1;
        for (int i = 19; i <= 34; i++) {
            if (container.getItem(i).hasFoil()) {
                glintIndex = i;
                break;
            }
        }
        if (glintIndex == -1) { // no glint item
            isShowingGlint = false;
        } else if (!isShowingGlint || (!memory.isEmpty() && memory.getLast() != glintIndex)) {
            isShowingGlint = true;
            memory.add(glintIndex);
            //GhostifyClient.LOGGER.debug("Chronomatron recognized: Slot {}", glintIndex);
        }
    }

    @Override
    public void onRemove(Screen screen) {
        AbstractExperimentSolver.ACTIVE_SOLVER = null;
        isShowingGlint = false;
        memory.clear();
        state = ExperimentState.INITIALIZE;
        GhostifyClient.LOGGER.info("Chronomatron stopped.");
    }
}
