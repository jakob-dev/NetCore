package de.jakob.netcore.velocity.config;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;

    private final Map<String, ConfigurationNode> configCache = new HashMap<>();

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            logger.error("Error trying to create config directory!", e);
        }
    }

    public ConfigurationNode getConfig(String fileName) {
        if (configCache.containsKey(fileName)) {
            return configCache.get(fileName);
        }
        return reloadConfig(fileName);
    }

    public ConfigurationNode reloadConfig(String fileName) {
        Path configFile = dataDirectory.resolve(fileName);

        if (!Files.exists(configFile)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
                if (in != null) {
                    Files.copy(in, configFile);
                } else {
                    Files.createFile(configFile);
                }
            } catch (IOException e) {
                logger.error("Error trying to create " + fileName, e);
            }
        }

        YamlConfigurationLoader configurationLoader = YamlConfigurationLoader.builder()
                .path(configFile)
                .build();

        try {
            ConfigurationNode node = configurationLoader.load();
            configCache.put(fileName, node);
            return node;
        } catch (IOException e) {
            logger.error("Error trying to load " + fileName, e);
            return null;
        }
    }

    public void saveConfig(String fileName) {
        if (!configCache.containsKey(fileName)) return;

        Path configFile = dataDirectory.resolve(fileName);
        YamlConfigurationLoader configurationLoader = YamlConfigurationLoader.builder()
                .path(configFile)
                .build();

        try {
            configurationLoader.save(configCache.get(fileName));
        } catch (IOException e) {
            logger.error("Error trying to save " + fileName, e);
        }
    }
}