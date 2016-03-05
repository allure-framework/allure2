package org.allurefw.report;

import com.google.inject.AbstractModule;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class ReadModule extends AbstractModule {

    private final Path[] inputDirectories;

    private final List<AbstractPlugin> readPlugins;

    public ReadModule(Path[] inputDirectories, List<AbstractPlugin> readPlugins) {
        this.inputDirectories = inputDirectories;
        this.readPlugins = readPlugins;
    }

    @Override
    protected void configure() {
        bind(Path[].class).annotatedWith(ResultsDirectories.class).toInstance(inputDirectories);
        readPlugins.forEach(this::install);
    }
}
