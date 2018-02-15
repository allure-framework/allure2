package io.qameta.allure.service;

import io.qameta.allure.entity.AttachmentLink;
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

    default List<TestResult> findAll() {
        return findAll(false);
    }

    List<TestResult> findAll(boolean includeHidden);

    default List<TestResult> findAllTests() {
        return findAllTests(false);
    }

    List<TestResult> findAllTests(boolean includeHidden);

    List<AttachmentLink> findAttachments(Long id);

    List<TestResult> findHistory(Long id);

    List<TestResult> findRetries(Long id);

    List<TestResult> findAllParents(Long id);

}
