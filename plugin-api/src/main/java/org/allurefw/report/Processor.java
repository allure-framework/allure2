package org.allurefw.report;

import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
@FunctionalInterface
public interface Processor {

    TestCaseResult process(TestRun testRun, TestCase testCase, TestCaseResult result);

}
