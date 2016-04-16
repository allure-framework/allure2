package org.allurefw.report;

import org.allurefw.report.entity.TestCase;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 16.04.16
 */
public interface Processor {

    TestCase process(TestCase testCase);

}
