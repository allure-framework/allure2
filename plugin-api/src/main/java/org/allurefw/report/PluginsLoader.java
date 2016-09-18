package org.allurefw.report;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface PluginsLoader {

    List<Plugin> loadPlugins();

}
