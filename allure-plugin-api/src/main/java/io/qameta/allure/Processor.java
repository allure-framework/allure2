package io.qameta.allure;

import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
@FunctionalInterface
public interface Processor {

    void process(TestRun testRun, TestCase testCase, TestCaseResult result);

}
