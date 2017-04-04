package io.qameta.allure.plugins;

import io.qameta.allure.core.PluginDescriptor;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface PluginsLoader {

    List<PluginDescriptor> loadPlugins();

}
