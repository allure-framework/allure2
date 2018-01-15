package io.qameta.allure.attachment;

import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class AllureAttachmentsReader implements ResultsReader {

    @Override
    public void readResults(final ResultsVisitor visitor, final Path resultsFile) {
        if (resultsFile.getFileName().toString().matches(".*-attachment\\..*")) {
            visitor.visitAttachmentFile(resultsFile);
        }
    }
}

