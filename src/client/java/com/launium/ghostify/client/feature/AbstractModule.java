package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.ui.modulelist.ModuleListContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractModule implements Comparable<AbstractModule> {
    public abstract String title();

    public abstract @Nullable String subtitle();

    public abstract boolean isActive();

    public ModuleListContext moduleList = new ModuleListContext();

    @Override
    public int compareTo(@NonNull AbstractModule another) {
        if (this == another) return 0;
        String anotherTitle = another.title();
        String anotherSubtitle = another.subtitle();
        String thisTitle = this.title();
        String thisSubtitle = this.subtitle();
        int v = Integer.compare(anotherTitle.length() + (anotherSubtitle == null ? 0 : anotherSubtitle.length()),
                thisTitle.length() + (thisSubtitle == null ? 0 : thisSubtitle.length()));
        if (v == 0) v = thisTitle.compareTo(anotherTitle);
        if (v == 0) v = Integer.compare(this.hashCode(), another.hashCode());
        return v;
    }
}