package org.allurefw.report;

import ru.qatools.properties.DefaultValue;
import ru.qatools.properties.Property;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.10.15
 */
public interface ReportConfig {

    /**
     * Pattern containing issue tracker base URL and one %s placeholder which will be replaced by issue name.
     * Example: http://example.com/%s and @Issue("SOME-123") will give you http://example.com/SOME-123
     */
    @DefaultValue("%s")
    @Property("allure.issues.tracker.pattern")
    String getIssueTrackerPattern();

    /**
     * Pattern containing Test Management System (TMS) and one %s placeholder which will be replaced by test id.
     * Currently there is no special annotation to define TMS test id in your tests. Work in progress...
     * Example: http://example.com/%s and <label name="testId" value="SOME-123"/> will give you http://example.com/SOME-123
     */
    @DefaultValue("%s")
    @Property("allure.tests.management.pattern")
    String getTmsPattern();

    @DefaultValue("true")
    @Property("allure.failIfNoResultsFound")
    boolean isFailIfNoResultsFound();
}
