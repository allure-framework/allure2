package org.allurefw.report;

import com.google.inject.Module;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class Plugin {

    private final PluginDescriptor descriptor;

    private final Path archive;

    private final Module module;

    public Plugin(PluginDescriptor descriptor, Module module, Path archive) {
        this.descriptor = descriptor;
        this.archive = archive;
        this.module = module;
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

    public Path getArchive() {
        return archive;
    }
}
