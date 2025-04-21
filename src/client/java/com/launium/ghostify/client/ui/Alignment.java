package com.launium.ghostify.client.ui;

public enum Alignment {
    START, CENTER, END;

    public float calculate(float start, float end, float size) {
        switch (this) {
            case START -> {
                return start;
            }
            case END -> {
                return end - size;
            }
            case CENTER -> {
                return (start + end - size) * 0.5F;
            }
        }
        throw new IllegalStateException();
    }
}
