package io.qameta.allure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.qameta.allure.CommandlineConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author charlie (Dmitry Baev).
 */
public class ConfigLoader {

    private final Path home;

    private final String profile;

    public ConfigLoader(final Path home, final String profile) {
        this.home = home;
        this.profile = profile;
    }

    public CommandlineConfig load() throws IOException {
        final Path configFile = home.resolve(getConfigPath());
        if (Files.notExists(configFile)) {
            return new CommandlineConfig();
        }
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = Files.newInputStream(configFile)) {
            return mapper.readValue(is, CommandlineConfig.class);
        }
    }

    private String getConfigPath() {
        return Objects.isNull(profile) || profile.isEmpty()
                ? "config/allure.yml"
                : String.format("config/allure-%s.yml", profile);
    }
}
