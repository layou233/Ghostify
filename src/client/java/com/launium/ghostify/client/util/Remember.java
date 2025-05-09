package com.launium.ghostify.client.util;

import java.util.Objects;

public class Remember<T> {
    private T value;

    public Remember() {
    }

    public Remember(T value) {
        this.value = value;
    }

    /**
     * @param newValue
     * @return If value is the same. Compare with "==" operator.
     */
    public boolean update(T newValue) {
        boolean ret = newValue == this.value;
        if (!ret) this.value = newValue;
        return ret;
    }

    /**
     * @param newValue
     * @return If value is the same. Compare with {@link Objects#equals}.
     */
    public boolean updateObject(T newValue) {
        boolean ret = Objects.equals(newValue, this.value);
        this.value = newValue; // hopefully to remove the old reference
        return ret;
    }
}
