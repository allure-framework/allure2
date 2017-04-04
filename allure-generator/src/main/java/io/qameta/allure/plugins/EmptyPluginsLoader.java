package io.qameta.allure.plugins;

import io.qameta.allure.core.PluginDescriptor;

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
