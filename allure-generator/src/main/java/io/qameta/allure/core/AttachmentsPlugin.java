package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.entity.Attachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
public class AttachmentsPlugin implements Aggregator {

    @Override
    public void aggregate(final ReportConfiguration configuration,
                          final List<LaunchResults> launches,
                          final Path outputDirectory) throws IOException {
        final Path attachmentsFolder = Files.createDirectories(outputDirectory.resolve("data/attachments"));
        for (LaunchResults launch : launches) {
            for (Map.Entry<Path, Attachment> entry : launch.getAttachments().entrySet()) {
                final Path file = attachmentsFolder.resolve(entry.getValue().getSource());
                Files.copy(entry.getKey(), file);
            }
        }
    }
}
