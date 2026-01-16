package com.launium.ghostify.client.ui.animation;

import net.minecraft.util.Mth;

public class Smooth extends Animation {
    public Smooth(float current, float target) {
        this.current = current;
        this.target = target;
    }

    @Override
    public float tick(float delta) {
        current = Mth.clampedLerp(delta, current, target);
        return current;
    }
}
