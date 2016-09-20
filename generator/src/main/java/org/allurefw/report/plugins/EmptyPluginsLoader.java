package org.allurefw.report.plugins;

import org.allurefw.report.Plugin;
import org.allurefw.report.PluginsLoader;

import java.util.Collections;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class EmptyPluginsLoader implements PluginsLoader {

    @Override
    public List<Plugin> loadPlugins() {
        return Collections.emptyList();
    }
}
