package com.launium.ghostify.client.ui.clickgui.fubuki.nav;

public interface NavigationDestination {
    String name();

    /**
     * @return if navigation success
     */
    boolean navigate();
}
