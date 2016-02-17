package org.allurefw.report;

import org.allurefw.report.entity.TestCase;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public interface TestCasePreparer {

    void prepare(TestCase testCase);

}
