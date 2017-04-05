package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.util.List;

/**
 * Widget extension.
 *
 * @since 2.0
 */
public interface Widget extends Extension {

    /**
     * Returns widget data. The data will be marshaled to JSON and
     * available in frontend widget plugin. To disable the widget return null.
     *
     * @param configuration the report configuration.
     * @param launches      the parsed tests results.
     * @return widget data. Null if widget is disabled.
     */
    Object getData(Configuration configuration, List<LaunchResults> launches);

    /**
     * Returns unique widget name. Should not be a null.
     *
     * @return widget name.
     */
    String getName();

}
