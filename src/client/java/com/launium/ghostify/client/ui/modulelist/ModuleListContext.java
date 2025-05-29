package com.launium.ghostify.client.ui.modulelist;

import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;

public class ModuleListContext {
    public boolean initializeAnimation = false;
    public Animation animatedX = new Smooth(0f, 0f);
    public Animation animatedY = new Smooth(0f, 0f);
}
