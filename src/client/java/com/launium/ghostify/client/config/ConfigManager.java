package com.launium.ghostify.client.config;

import com.launium.ghostify.client.GhostifyClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class ConfigManager {
    public static GeneralConfig GENERAL = loadFile(GeneralConfig.class, GeneralConfig.CONFIG_NAME, GeneralConfig::new);
    public static FeaturesConfig FEATURES = loadFile(FeaturesConfig.class, FeaturesConfig.CONFIG_NAME, FeaturesConfig::new);
    public static PatchesConfig PATCHES = loadFile(PatchesConfig.class, PatchesConfig.CONFIG_NAME, PatchesConfig::new);
    public static CommandsConfig COMMANDS = loadFile(CommandsConfig.class, CommandsConfig.CONFIG_NAME, CommandsConfig::new);

    private static <T extends AbstractConfig> T loadFile(Class<T> clazz, String configName, Supplier<T> factory) {
        try {
            Files.createDirectories(Path.of("config/Ghostify"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader fileReader = Files.newBufferedReader(Path.of("config/Ghostify/" + configName))) {
            return GhostifyClient.GSON.fromJson(fileReader, clazz);
        } catch (IOException e) {
            GhostifyClient.LOGGER.error("[Ghostify Config Manager] Failed to load {}, use default instead.", configName, e);
            T newInstance = factory.get();
            if (e instanceof NoSuchFileException) { // create a new one
                try {
                    newInstance.save();
                } catch (IOException ignored) {
                }
            }
            return newInstance;
        }
    }

    public static void processChanges() {
        GENERAL.processChanges();
        FEATURES.processChanges();
        PATCHES.processChanges();
        COMMANDS.processChanges();
    }

    public static void init() {
    }
}
