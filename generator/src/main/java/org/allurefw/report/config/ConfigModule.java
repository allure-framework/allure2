package org.allurefw.report.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.allurefw.report.ReportConfig;
import ru.qatools.properties.PropertyLoader;

import java.nio.file.Path;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
public class ConfigModule extends AbstractModule {

    private final Path[] inputDirectories;

    public ConfigModule(Path... inputDirectories) {
        this.inputDirectories = inputDirectories;
    }

    @Override
    protected void configure() {
        //do nothing
    }

    @Provides
    @Singleton
    protected ReportConfig index() {
        return PropertyLoader.newInstance().withPropertyProvider(
                new AllurePropertyProvider(inputDirectories)
        ).populate(ReportConfig.class);
    }
}
