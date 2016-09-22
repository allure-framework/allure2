package org.allurefw.report;

import ru.qatools.properties.Property;
import ru.qatools.properties.Resource;

import java.nio.file.Path;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@Resource.Classpath({"command.properties"})
public interface CommandProperties {

    @Property("allure.home")
    Path getAllureHome();

}
