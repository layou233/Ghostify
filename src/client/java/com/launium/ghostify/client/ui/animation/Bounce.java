package com.launium.ghostify.client.ui.animation;

import net.minecraft.util.Mth;

public class Bounce extends Animation {
    private boolean bouncing = false;
    private float bounceTarget;
    private float distance = 0f;

    public Bounce(float current, float target) {
        this.current = current;
        this.target = target;
    }

    @Override
    public void update(float newTarget) {
        float oldTarget = this.target;
        // do bouncing when having diff greater than 5
        if (Math.abs(newTarget - oldTarget) > 5f) {
            distance = (newTarget - current) * 0.1f;
            bounceTarget = current - distance;
            distance = Math.abs(distance);
            if (bounceTarget > 0) bouncing = true;
        }
        super.update(newTarget);
    }

    @Override
    public float tick(float delta) {
        if (bouncing) {
            current = Mth.clampedLerp(current, bounceTarget, delta * 2f);
            if (Math.abs(current - bounceTarget) < distance * 0.2f) {
                bouncing = false; // done
            }
        } else {
            current = Mth.clampedLerp(current, target, delta);
        }
        return current;
    }
}
