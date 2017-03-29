package io.qameta.allure;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface PluginsLoader {

    List<PluginDescriptor> loadPlugins();

}
