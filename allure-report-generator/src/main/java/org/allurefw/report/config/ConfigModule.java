package org.allurefw.report.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.allurefw.report.ReportConfig;
import org.allurefw.report.ResultDirectories;
import ru.qatools.properties.PropertyLoader;

import java.nio.file.Path;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class ConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        //do nothing
    }

    @Provides
    @Singleton
    protected ReportConfig index(@ResultDirectories Path... inputDirectories) {
        return PropertyLoader.newInstance().withPropertyProvider(
                new AllurePropertyProvider(inputDirectories)
        ).populate(ReportConfig.class);
    }
}
