package org.allurefw.report;

import com.google.inject.Guice;
import com.google.inject.Module;
import org.allurefw.report.plugins.DefaultPluginsLoader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class Main {

    private final Configuration configuration;

    public Main(Configuration configuration) {
        this.configuration = configuration;
    }

    public List<Plugin> loadPlugins() {
        PluginsLoader pluginsLoader = new DefaultPluginsLoader(
                configuration.getPluginsDirectory(),
                configuration.getWorkDirectory()
        );
        return pluginsLoader.loadPlugins();
    }

    public boolean isPluginEnabled(Plugin plugin) {
        return configuration.getEnabledPlugins()
                .contains(plugin.getDescriptor().getName());
    }

    public List<Plugin> getEnabledPlugins() {
        return loadPlugins().stream()
                .filter(this::isPluginEnabled)
                .collect(Collectors.toList());
    }

    public List<Module> getEnabledPluginsModules() {
        return getEnabledPlugins().stream()
                .map(Plugin::getModule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Report createReport(ResultsSource... sources) {
        ReportFactory factory = Guice.createInjector(new ParentModule(loadPlugins()))
                .createChildInjector(getEnabledPluginsModules())
                .getInstance(ReportFactory.class);
        return factory.create(sources);
    }

    public static void main(String[] args) {
        new Main(null).createReport();
    }
}
