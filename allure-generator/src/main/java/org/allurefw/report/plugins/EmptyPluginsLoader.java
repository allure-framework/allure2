package org.allurefw.report.plugins;

import org.allurefw.report.Plugin;
import org.allurefw.report.PluginsLoader;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class EmptyPluginsLoader implements PluginsLoader {

    @Override
    public List<Plugin> loadPlugins(Set<String> enabledPlugins) {
        return Collections.emptyList();
    }
}
