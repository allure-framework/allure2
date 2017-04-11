package io.qameta.allure.core;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.Map;

/**
 * Visitor that stores results data to internal storage.
 * Creates {@link LaunchResults} from all visited data.
 *
 * @since 2.0
 */
public interface ResultsVisitor {

    Attachment visitAttachmentFile(Path attachmentFile);

    void visitTestCase(TestCase testCase);

    void visitTestResult(TestCaseResult result);

    void visitConfiguration(Map<String, String> properties);

    void visitExtra(String name, Object object);

    void error(String message, Exception e);

    void error(String message);

    LaunchResults getLaunchResults();

}
