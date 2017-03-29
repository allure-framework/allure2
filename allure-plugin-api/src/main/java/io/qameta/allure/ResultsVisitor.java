package io.qameta.allure;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
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
