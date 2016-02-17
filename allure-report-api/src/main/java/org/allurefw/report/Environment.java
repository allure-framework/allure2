package org.allurefw.report;

import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 05.10.15
 */
public interface Environment {

    /**
     * Returns the name of the test run. Default value is <code>Allure Test Run</code>
     */
    String getName();

    /**
     * Returns the url to the test run in test execution system.
     */
    String getUrl();

    /**
     * Returns the id of the test run.
     */
    String getId();

    /**
     * Returns the test run environment variables.
     */
    Map<String, String> getParameters();
}
