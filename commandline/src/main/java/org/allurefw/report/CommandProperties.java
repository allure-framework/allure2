package org.allurefw.report;

import ru.qatools.properties.DefaultValue;
import ru.qatools.properties.Property;
import ru.qatools.properties.Required;
import ru.qatools.properties.Resource;

import java.nio.file.Path;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@Resource.Classpath({"command.properties"})
public interface CommandProperties {

    @Required
    @Property("os.name")
    String getOsName();

    @Required
    @Property("java.home")
    Path getJavaHome();

    @Required
    @Property("allure.home")
    Path getAllureHome();

    @DefaultValue("allure.properties")
    @Property("allure.config")
    Path getAllureConfig();

    @DefaultValue("-Xms128m")
    @Property("allure.bundle.javaOpts")
    String getBundleJavaOpts();

}
