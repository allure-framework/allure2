package io.qameta.allure.service;

import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;

import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestResultService {

    TestResult create(TestResult testResult);

    void addExecution(Long testResultId, TestResultExecution execution);

    Optional<TestResultExecution> findExecution(Long id);

    Optional<TestResult> findOneById(Long id);

    List<TestResult> findAll();

    List<TestResult> findAllTests();

    List<TestResult> findHistory(Long id);

    List<TestResult> findRetries(Long id);

    List<TestResult> findAllParents(Long id);

}
