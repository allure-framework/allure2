package io.qameta.allure.writer;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCaseResult;

import java.io.InputStream;

/**
 * @author charlie (Dmitry Baev).
 */
public interface ReportWriter {

    void writeTestCase(TestCaseResult result);

    void writeAttachment(InputStream attachmentBody, Attachment attachment);

    void writeJsonData(String fileName, Object data);

}
