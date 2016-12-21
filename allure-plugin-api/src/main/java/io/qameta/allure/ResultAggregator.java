package io.qameta.allure;

import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 04.03.16
 */
public interface ResultAggregator<T> {

    Supplier<T> supplier(TestRun testRun, TestCase testCase);

    Consumer<T> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result);

}
