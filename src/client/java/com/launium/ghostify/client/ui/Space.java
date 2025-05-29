package com.launium.ghostify.client.ui;

import net.minecraft.util.Mth;

public class Space {
    protected final float width;
    protected final float height;

    public float left;
    public float right;
    public float top;
    public float bottom;

    public Space(float width, float height) {
        this.width = width;
        this.height = height;
        this.left = 0;
        this.top = 0;
        this.right = width;
        this.bottom = height;
    }

    public float borrowVertical(Alignment alignment, float expectedSize, float padding) {
        float pos = alignment.calculate(top, bottom, expectedSize, padding);
        return Mth.clamp(pos, top, bottom);
    }

    public float allocateVertical(Alignment alignment, float expectedSize, float padding) {
        float pos = borrowVertical(alignment, expectedSize, padding);
        switch (alignment) {
            case START -> top = pos + expectedSize;
            case END -> bottom = pos;
        }
        return pos;
    }

    public float borrowHorizontal(Alignment alignment, float expectedSize, float padding) {
        float pos = alignment.calculate(left, right, expectedSize, padding);
        return Mth.clamp(pos, left, right);
    }

    public float allocateHorizontal(Alignment alignment, float expectedSize, float padding) {
        float pos = borrowHorizontal(alignment, expectedSize, padding);
        switch (alignment) {
            case START -> left = pos + expectedSize;
            case END -> right = pos;
        }
        return pos;
    }
}
