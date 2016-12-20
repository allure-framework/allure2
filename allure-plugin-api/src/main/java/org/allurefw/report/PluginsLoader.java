package org.allurefw.report;

import java.util.List;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface PluginsLoader {

    List<Plugin> loadPlugins(Set<String> enabledPlugins);

}
