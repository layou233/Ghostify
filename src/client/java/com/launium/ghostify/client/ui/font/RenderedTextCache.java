package com.launium.ghostify.client.ui.font;

import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair;

import java.util.HashMap;

public class RenderedTextCache {
    // <text, size> to <cache, isVisited>
    static HashMap<RenderInfo, ObjectBooleanMutablePair<RenderedText>> CACHE = new HashMap<>();

    static RenderedText get(RenderInfo info) {
        return CACHE
                .computeIfAbsent(info,
                        key -> ObjectBooleanMutablePair.of(RenderedText.create(key), true))
                .right(true)
                .left();
    }

    public static void whenRenderEnd() {
        CACHE.entrySet().removeIf(entry -> {
            ObjectBooleanMutablePair<RenderedText> v = entry.getValue();
            if (v.rightBoolean()) { // is just visited?
                v.right(false); // reset and keep it for next round
                return false;
            } else {
                v.left().close();
                return true; // remove unused cache
            }
        });
    }
}
