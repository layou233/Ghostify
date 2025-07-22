package com.launium.ghostify.client.ui.clickgui.fubuki.nav;

@FunctionalInterface
public interface DummyNavigationDestination extends NavigationDestination {
    @Override
    default boolean navigate() {
        return false;
    }
}
