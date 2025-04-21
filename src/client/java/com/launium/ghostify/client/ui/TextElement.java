package com.launium.ghostify.client.ui;

import org.jetbrains.annotations.NotNull;

public class TextElement {
    public String text;
    public Alignment align = Alignment.CENTER;
    public int color = Easy2D.TEXT_DEFAULT_COLOR; // ARGB

    public TextElement(@NotNull String text) {
        this.text = text;
    }

    public TextElement startFrom(Alignment align) {
        this.align = align;
        return this;
    }

    public TextElement color(int color) {
        this.color = color;
        return this;
    }
}
