package com.launium.ghostify.client.util;

import com.launium.ghostify.client.GhostifyClient;

import java.io.IOException;

public class PlatformNotification {
    private static final String TITLE = "Ghostify Notification";

    private static final OS os = OS.get();
    private static volatile boolean failed;

    private enum OS {
        OTHER, WINDOWS, MACOS, LINUX;

        public static OS get() {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                return WINDOWS;
            } else if (os.contains("mac")) {
                return MACOS;
            } else if (os.contains("linux")) {
                return LINUX;
            }
            return OTHER;
        }
    }

    public static void show(String title, String message) {
        if (failed) { // won't try again
            GhostifyClient.LOGGER.info("[Ghostify Stub Notifier] {}: {}", title, message);
            return;
        }

        // we have to use all these shit since AWT is working on headless mode in Minecraft
        switch (os) {
            case WINDOWS ->
                    showNotificationWithCommand(new String[]{"powershell.exe", "-NoProfile", "-Command", getWindowsToastScript(title, message)},
                            title, message);
            case MACOS -> showNotificationWithCommand(new String[]{
                            "osascript", "-e",
                            "display notification \"" + escape(message) +
                                    "\" with title \"" + TITLE +
                                    "\" subtitle \"" + escape(title) + '"'},
                    title, message);
            case LINUX -> // requires libnotify
                    showNotificationWithCommand(new String[]{"notify-send", "-a", TITLE, "-t", "5000", title, message},
                            title, message);
            default -> GhostifyClient.LOGGER.info("[Ghostify Stub Notifier] {}: {}", title, message);
        }
    }

    private static String getWindowsToastScript(String title, String message) {
        return "Add-Type -A 'Windows.Data,ContentType=WindowsRuntime'" + // for legacy compatibility
                "Add-Type -A 'System.Runtime.WindowsRuntime';" +
                "$null=[Windows.UI.Notifications.ToastNotificationManager,Windows.UI.Notifications,ContentType=WindowsRuntime];" +
                "$xml=[Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent(5);" + // 5 == [Windows.UI.Notifications.ToastTemplateType]::ToastText02
                "$t=$xml.GetElementsByTagName('text');" +
                "$t.Item(0).AppendChild($xml.CreateTextNode('" + escapePowershell(title) + "'))> $null;" +
                "$t.Item(1).AppendChild($xml.CreateTextNode('" + escapePowershell(message) + "'))> $null;" +
                "$toast=[Windows.UI.Notifications.ToastNotification]::new($xml);" +
                "$toast.ExpirationTime = [DateTimeOffset]::Now.AddSeconds(5);" +
                "[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('" + TITLE + "').Show($toast);";
    }

    private static void showNotificationWithCommand(String[] cmd, String title, String message) {
        Thread.startVirtualThread(() -> {
            try {
                runCommand(cmd);
            } catch (Exception e) {
                PlatformNotification.failed = true;
                GhostifyClient.LOGGER.error("[Ghostify Notifier] {}: {}; error: {}", title, message, e);
            }
        });
    }

    private static void runCommand(String[] cmd) throws IOException, InterruptedException {
        new ProcessBuilder(cmd).start().waitFor();
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }

    private static String escapePowershell(String string) {
        return escape(string).replace("'", "''");
    }
}
