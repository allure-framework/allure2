/*
 *  Copyright 2016-2026 Qameta Software Inc
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
package io.qameta.allure.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.GlobalAttachment;
import io.qameta.allure.entity.GlobalError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.qameta.allure.detect.WellKnownFileExtensionsUtils.getExtensionByMimeType;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Objects.nonNull;

/**
 * Reads run-level errors and attachments from Allure 2 globals files.
 */
class Allure2GlobalsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2GlobalsReader.class);

    private static final String TEMPORARY_FILE_SUFFIX = ".tmp";

    private static final Pattern ATTACHMENT_SOURCE_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{1,100}$");

    private final ObjectMapper mapper;

    Allure2GlobalsReader(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    void readResults(final ResultsVisitor visitor, final Path resultsDirectory) {
        listFiles(resultsDirectory)
                .sorted()
                .map(this::readGlobalsFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(globals -> processGlobals(resultsDirectory, visitor, globals));
    }

    private void processGlobals(final Path resultsDirectory,
                                final ResultsVisitor visitor,
                                final Allure2Globals globals) {
        Optional.ofNullable(globals.getErrors())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(
                        error -> new GlobalError()
                                .setTimestamp(error.getTimestamp())
                                .setMessage(error.getMessage())
                                .setTrace(error.getTrace())
                                .setActual(error.getActual())
                                .setExpected(error.getExpected())
                )
                .forEach(visitor::visitGlobalError);

        Optional.ofNullable(globals.getAttachments())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(Objects::nonNull)
                .forEach(attachment -> processGlobalAttachment(resultsDirectory, visitor, attachment));
    }

    private void processGlobalAttachment(final Path resultsDirectory,
                                         final ResultsVisitor visitor,
                                         final Allure2Globals.Attachment attachment) {
        final String attachmentSource = attachment.getSource();
        if (!isValidAttachmentFileName(attachmentSource)) {
            visitor.error("Invalid global attachment source is provided: " + attachmentSource);
            return;
        }

        final Path normalizedSource = resultsDirectory.normalize();
        final Path attachmentFile = normalizedSource.resolve(attachmentSource).normalize();
        if (attachmentSource.endsWith(TEMPORARY_FILE_SUFFIX)
                || !attachmentFile.startsWith(normalizedSource)
                || !Files.isRegularFile(attachmentFile, LinkOption.NOFOLLOW_LINKS)) {
            visitor.error("Could not find global attachment " + attachmentSource + " in directory " + normalizedSource);
            return;
        }

        final Attachment stored = visitor.visitAttachmentFile(attachmentFile);
        if (nonNull(attachment.getType())) {
            stored.setType(attachment.getType());
            final String ext = getExtensionByMimeType(attachment.getType());
            if (!ext.isEmpty()) {
                stored.setSource(stored.getUid() + "." + ext);
            }
        }

        visitor.visitGlobalAttachment(
                new GlobalAttachment()
                        .setTimestamp(attachment.getTimestamp())
                        .setUid(stored.getUid())
                        .setName(Optional.ofNullable(attachment.getName()).orElse(stored.getName()))
                        .setSource(stored.getSource())
                        .setType(stored.getType())
                        .setSize(stored.getSize())
        );
    }

    private Optional<Allure2Globals> readGlobalsFile(final Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return Optional.ofNullable(mapper.readValue(is, Allure2Globals.class));
        } catch (IOException e) {
            LOGGER.error("Could not read globals file {}", file, e);
            return Optional.empty();
        }
    }

    private Stream<Path> listFiles(final Path directory) {
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, "*-globals.json")) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().endsWith(TEMPORARY_FILE_SUFFIX))
                    .collect(Collectors.toList())
                    .stream();
        } catch (IOException e) {
            LOGGER.error("Could not list globals files in directory {}", directory, e);
            return Stream.empty();
        }
    }

    private static boolean isValidAttachmentFileName(final String fileName) {
        return nonNull(fileName) && ATTACHMENT_SOURCE_PATTERN.matcher(fileName).matches();
    }

}
