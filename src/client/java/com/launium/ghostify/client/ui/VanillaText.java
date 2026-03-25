package com.launium.ghostify.client.ui;

import org.jspecify.annotations.NonNull;

public class VanillaText {
    public String text;
    public Alignment align = Alignment.CENTER;
    public int color = Easy2D.TEXT_DEFAULT_COLOR; // ARGB

    public VanillaText(@NonNull String text) {
        this.text = text;
    }

    public VanillaText startFrom(Alignment align) {
        this.align = align;
        return this;
    }

    public VanillaText color(int color) {
        this.color = color;
        return this;
    }
}
