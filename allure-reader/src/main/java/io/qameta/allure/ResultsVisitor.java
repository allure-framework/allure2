package io.qameta.allure;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;

import java.nio.file.Path;

/**
 * Visitor that stores results data to internal storage.
 *
 * @since 2.0
 */
public interface ResultsVisitor {

    /**
     * Process test result.
     *
     * @param result the result to process.
     */
    TestResult visitTestResult(TestResult result);

    /**
     * Process test results's execution.
     *
     * @param testResultId the id of test result to process.
     * @param execution    the execution of test result.
     */
    TestResultExecution visitTestResultExecution(Long testResultId, TestResultExecution execution);

    /**
     * Process attachment link.
     *
     * @param testResultId the result that contains link to the attachment.
     * @param link         the link.
     */
    AttachmentLink visitAttachmentLink(Long testResultId, AttachmentLink link);

    /**
     * Process attachment file. Returns {@link Attachment} that can be
     * used to get attachment in the report.
     *
     * @param attachmentFile the attachment file to process.
     * @return created {@link Attachment}.
     */
    Attachment visitAttachmentFile(Path attachmentFile);

}
