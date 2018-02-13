package io.qameta.allure.attachment;

import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.util.FileUtils;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class AllureAttachmentsReader implements ResultsReader {

    @SuppressWarnings("all")
    @Override
    public void readResultFile(final ResultsVisitor visitor, final Path file) {
        if (FileUtils.matches(file, ".*-attachment\\..*")) {
            visitor.visitAttachmentFile(file);
        }
    }
}

