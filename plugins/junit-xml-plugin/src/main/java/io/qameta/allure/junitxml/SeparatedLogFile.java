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
package io.qameta.allure.junitxml;

import io.qameta.allure.entity.Status;
import io.qameta.allure.parser.XmlElement;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.junitxml.JunitXmlConstants.CLASS_NAME_ATTRIBUTE_NAME;
import static io.qameta.allure.junitxml.JunitXmlConstants.ERROR_ELEMENT_NAME;
import static io.qameta.allure.junitxml.JunitXmlConstants.FAILURE_ELEMENT_NAME;
import static io.qameta.allure.junitxml.JunitXmlConstants.NAME_ATTRIBUTE_NAME;
import static io.qameta.allure.junitxml.JunitXmlConstants.SKIPPED_ATTRIBUTE_VALUE;
import static io.qameta.allure.junitxml.JunitXmlConstants.SKIPPED_ELEMENT_NAME;
import static io.qameta.allure.junitxml.JunitXmlConstants.STATUS_ATTRIBUTE_NAME;
import static io.qameta.allure.junitxml.JunitXmlConstants.TXT_EXTENSION;

public class SeparatedLogFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeparatedLogFile.class);
    @Getter
    private static final String PREFIX = "separated_log_file_";
    private static final String NAME_SEPARATOR = "_";
    private static final String FAILURE_LOG_INDICATOR = "<<< FAILURE! ";
    private final XmlElement xmlElement;
    private final String className;
    private final Path resultsDirectory;
    private final String tmpFileName;
    private Path tmpFile;
    @Getter
    private final Status status;

    SeparatedLogFile(final XmlElement xmlElement, final Path resultDirectory) {
        this.xmlElement = xmlElement;
        this.resultsDirectory = resultDirectory;
        final String testName = xmlElement.getAttribute(NAME_ATTRIBUTE_NAME);
        className = xmlElement.getAttribute(CLASS_NAME_ATTRIBUTE_NAME);
        status = getStatus(xmlElement);
        tmpFileName = ((status.equals(Status.FAILED)) ? "" : "success_")
                + className + NAME_SEPARATOR + testName + TXT_EXTENSION;
        try {
            tmpFile = File.createTempFile(PREFIX, tmpFileName).toPath();
            tmpFile.toFile().deleteOnExit();
        } catch (IOException e) {
            LOGGER.error("Cannot create temporary file {}", tmpFileName);
        }
        writeToLogFile();
    }

    private void writeToLogFile() {
        try {
            if (status.equals(Status.FAILED)) {
                final String errorMessage = xmlElement.getFirst(FAILURE_ELEMENT_NAME).get().getValue();
                Files.write(tmpFile, errorMessage.getBytes(StandardCharsets.UTF_8));
            } else {
                final Path sourcePath = resultsDirectory.resolve(className + TXT_EXTENSION);
                try (BufferedReader br = Files.newBufferedReader(sourcePath)) {
                    final String successMessage = br.lines()
                            .limit(4)
                            .collect(Collectors.joining("\n"))
                            .replace(FAILURE_LOG_INDICATOR, "");
                    Files.write(tmpFile, successMessage.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Cannot separate log file for {}", tmpFileName);
        }
    }

    private Status getStatus(final XmlElement testCaseElement) {
        if (testCaseElement.contains(FAILURE_ELEMENT_NAME)) {
            return Status.FAILED;
        }
        if (testCaseElement.contains(ERROR_ELEMENT_NAME)) {
            return Status.BROKEN;
        }
        if (testCaseElement.contains(SKIPPED_ELEMENT_NAME)) {
            return Status.SKIPPED;
        }

        if ((testCaseElement.containsAttribute(STATUS_ATTRIBUTE_NAME))
                && (testCaseElement.getAttribute(STATUS_ATTRIBUTE_NAME).equals(SKIPPED_ATTRIBUTE_VALUE))) {
            return Status.SKIPPED;
        }

        return Status.PASSED;
    }

    public Optional<Path> getLogFile() {
        return Optional.of(tmpFile);
    }

}
