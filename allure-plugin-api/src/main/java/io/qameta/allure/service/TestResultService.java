package io.qameta.allure.service;

import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestResultService {

    TestResult create(TestResult testResult);

    TestResult update(TestResult testResult);

    Optional<TestResult> findOneById(Long id);

    List<TestResult> findAllTests();

}
