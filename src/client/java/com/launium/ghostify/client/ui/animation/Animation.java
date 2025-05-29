package com.launium.ghostify.client.ui.animation;

public abstract class Animation {
    public float current;
    public float target;

    public void reset(float current, float target) {
        this.current = current;
        this.target = target;
    }

    public void update(float newTarget) {
        target = newTarget;
    }

    public abstract float tick(float delta);

    public float ratio() {
        return current / target;
    }
}
