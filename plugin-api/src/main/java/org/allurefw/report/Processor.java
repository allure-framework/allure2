package org.allurefw.report;

import org.allurefw.report.entity.TestCaseResult;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
@FunctionalInterface
public interface Processor {

    TestCaseResult process(TestCaseResult testCase);

}
