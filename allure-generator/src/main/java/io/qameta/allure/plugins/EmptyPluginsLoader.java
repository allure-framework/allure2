package io.qameta.allure.plugins;

import io.qameta.allure.PluginDescriptor;
import io.qameta.allure.PluginsLoader;

import java.util.Collections;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class EmptyPluginsLoader implements PluginsLoader {

    @Override
    public List<PluginDescriptor> loadPlugins() {
        return Collections.emptyList();
    }
}
