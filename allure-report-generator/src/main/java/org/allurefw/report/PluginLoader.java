package org.allurefw.report;

import org.allurefw.report.allure1.Allure1Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class PluginLoader {

    public List<AbstractPlugin> loadPlugins(ClassLoader loader) {
        return Arrays.asList(
                new Allure1Plugin()
        );
    }

}
