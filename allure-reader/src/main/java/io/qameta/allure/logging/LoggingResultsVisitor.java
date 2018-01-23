package io.qameta.allure.logging;

import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author charlie (Dmitry Baev).
 */
public class LoggingResultsVisitor implements ResultsVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingResultsVisitor.class);

    protected final AtomicLong resultId = new AtomicLong();
    protected final AtomicLong attachmentId = new AtomicLong();

    @Override
    public TestResult visitTestResult(final TestResult result) {
        LOGGER.info("visit test result: {}", result);
        result.setId(resultId.incrementAndGet());
        return result;
    }

    @Override
    public TestResultExecution visitTestResultExecution(final Long testResultId, final TestResultExecution execution) {
        LOGGER.info("visit test result execution: {} {}", testResultId, execution);
        return execution;
    }

    @Override
    public AttachmentLink visitAttachmentLink(final Long testResultId, final AttachmentLink link) {
        LOGGER.info("visit attachment link: {} {}", testResultId, link);
        link.setId(attachmentId.incrementAndGet());
        return link;
    }

    @Override
    public Attachment visitAttachmentFile(final Path attachmentFile) {
        LOGGER.info("visit attachment file: {}", attachmentFile);
        return new Attachment();
    }
}
