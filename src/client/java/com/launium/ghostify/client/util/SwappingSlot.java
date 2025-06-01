package com.launium.ghostify.client.util;

public class SwappingSlot<T> {
    private T main;
    private T back;

    public SwappingSlot(T main, T back) {
        this.main = main;
        this.back = back;
    }

    public synchronized T get() {
        return main;
    }

    public synchronized T getBack() {
        return back;
    }

    public synchronized void swap() {
        T t = main;
        main = back;
        back = t;
    }
}
