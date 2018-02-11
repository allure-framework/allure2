package io.qameta.allure;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.service.TestResultService;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultResultsVisitor implements ResultsVisitor {

    private final TestResultService testResultService;

    public DefaultResultsVisitor(final TestResultService testResultService) {
        this.testResultService = testResultService;
    }

    @Override
    public TestResult visitTestResult(
            final TestResult result) {
        return testResultService.create(result);
    }

    @Override
    public TestResultExecution visitTestResultExecution(
            final Long testResultId, final TestResultExecution execution) {
        return null;
    }

    @Override
    public AttachmentLink visitAttachmentLink(
            final Long testResultId, final AttachmentLink link) {
        return null;
    }

    @Override
    public Attachment visitAttachmentFile(
            final Path attachmentFile) {
        return null;
    }
}
