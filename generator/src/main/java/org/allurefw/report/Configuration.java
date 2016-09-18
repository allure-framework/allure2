package org.allurefw.report;

import ru.qatools.properties.DefaultValue;
import ru.qatools.properties.Property;
import ru.qatools.properties.Required;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Configuration {

    @Required
    @Property("allure.report.pluginsDirectory")
    Path getPluginsDirectory();

    @Required
    @Property("allure.report.workDirectory")
    Path getWorkDirectory();

    @DefaultValue("")
    @Property("allure.report.enabledPlugins")
    Set<String> getEnabledPlugins();

}
