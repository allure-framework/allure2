package io.qameta.allure;

import com.google.inject.Module;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class Plugin {

    private final PluginDescriptor descriptor;

    private final Path pluginDirectory;

    private final Module module;

    private final boolean enabled;

    public Plugin(PluginDescriptor descriptor, Module module, Path pluginDirectory, boolean enabled) {
        this.descriptor = descriptor;
        this.pluginDirectory = pluginDirectory;
        this.module = module;
        this.enabled = enabled;
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public boolean hasModule() {
        return Objects.nonNull(module);
    }

    public Optional<Module> getModule() {
        return Optional.ofNullable(module);
    }

    public Path getPluginDirectory() {
        return pluginDirectory;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
