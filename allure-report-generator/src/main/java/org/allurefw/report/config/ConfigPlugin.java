package org.allurefw.report.config;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginScope;
import org.allurefw.report.ReportConfig;
import org.allurefw.report.ResultsDirectories;
import ru.qatools.properties.PropertyLoader;

import java.nio.file.Path;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "config-support", scope = PluginScope.CORE)
public class ConfigPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        //do nothing
    }

    @Provides
    @Singleton
    protected ReportConfig index(@ResultsDirectories Path... inputDirectories) {
        return PropertyLoader.newInstance().withPropertyProvider(
                new AllurePropertyProvider(inputDirectories)
        ).populate(ReportConfig.class);
    }
}
