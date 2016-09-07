package org.allurefw.report;

import org.allurefw.report.plugins.DefaultPluginsLoader;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportGenerator2 {

    private final Configuration configuration;

    private final ResultsSource[] resultsSource;

    public ReportGenerator2(Configuration configuration, ResultsSource... resultsSource) {
        this.configuration = configuration;
        this.resultsSource = resultsSource;
    }

    public List<Plugin> getInstalledPlugins() {
        PluginsLoader pluginsLoader = new DefaultPluginsLoader(
                configuration.getPluginsDirectory(),
                configuration.getWorkDirectory()
        );

        return pluginsLoader.loadPlugins();
    }
}
