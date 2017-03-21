package io.qameta.allure.plugins;

import io.qameta.allure.Plugin;
import io.qameta.allure.PluginsLoader;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class EmptyPluginsLoader implements PluginsLoader {

    @Override
    public List<Plugin> loadPlugins(final Set<String> enabledPlugins) {
        return Collections.emptyList();
    }
}
