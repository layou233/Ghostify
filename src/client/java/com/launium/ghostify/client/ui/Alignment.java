package com.launium.ghostify.client.ui;

public enum Alignment {
    START, CENTER, END;

    public float calculate(float start, float end, float size) {
        return calculate(start, end, size, 0f);
    }

    public float calculate(float start, float end, float size, float padding) {
        switch (this) {
            case START -> {
                return start + padding;
            }
            case END -> {
                return end - size - padding;
            }
            case CENTER -> {
                return (start + end - size) * 0.5F;
            }
        }
        throw new IllegalStateException();
    }
}
