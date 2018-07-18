package io.qameta.allure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.qameta.allure.CommandlineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class ConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    private final Path configFile;

    public ConfigLoader(final Path configFile) {
        this.configFile = configFile;
    }

    @SuppressWarnings("ReturnCount")
    public CommandlineConfig load() {
        if (Files.notExists(configFile)) {
            LOGGER.error("Could not find config file {}. Using the empty configuration", configFile);
            return new CommandlineConfig();
        }
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = Files.newInputStream(configFile)) {
            return mapper.readValue(is, CommandlineConfig.class);
        } catch (IOException e) {
            LOGGER.error("Could not load config file {}. Using the empty configuration", configFile);
            return new CommandlineConfig();
        }
    }
}
