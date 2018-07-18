package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.entity.Attachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

/**
 * Plugin that stores attachments to report data folder.
 *
 * @since 2.0
 */
public class AttachmentsPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final Path attachmentsFolder = Files.createDirectories(
                outputDirectory.resolve(Constants.DATA_DIR).resolve("attachments")
        );
        for (LaunchResults launch : launchesResults) {
            for (Map.Entry<Path, Attachment> entry : launch.getAttachments().entrySet()) {
                final Path file = attachmentsFolder.resolve(entry.getValue().getSource());
                Files.copy(entry.getKey(), file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
