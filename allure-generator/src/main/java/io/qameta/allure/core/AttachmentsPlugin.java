/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Constants;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Plugin that stores attachments to report data folder.
 *
 * @since 2.0
 */
public class AttachmentsPlugin implements Aggregator {

    @Override
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final Path attachmentsFolder = Files.createDirectories(
                outputDirectory.resolve(Constants.DATA_DIR).resolve("attachments")
        );
        launchesResults.forEach(launch -> launch.getAttachments().entrySet()
                .parallelStream()
                .forEach(
                        entry -> {
                            final Path file = attachmentsFolder.resolve(entry.getValue().getSource());
                            try {
                                Files.copy(entry.getKey(), file, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }));
    }
}
