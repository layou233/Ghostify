package com.launium.ghostify.client.config;

import com.launium.ghostify.client.GhostifyClient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractConfig {
    protected abstract String getConfigName();

    private transient boolean isChanged;

    public void save() throws IOException {
        try (BufferedWriter fileOut = Files.newBufferedWriter(Path.of("config/Ghostify/" + getConfigName()))) {
            GhostifyClient.GSON.toJson(this, fileOut);
        }
    }

    public void markAsChanged() {
        isChanged = true;
    }

    void processChanges() {
        if (isChanged) {
            isChanged = false;
            try {
                save();
            } catch (IOException e) {
                GhostifyClient.LOGGER.error("[Ghostify Config Manager] Failed to save {}", getConfigName(), e);
            }
        }
    }
}
