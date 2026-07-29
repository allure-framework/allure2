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
package io.qameta.allure;

import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.detect.MagicBytesContentTypeDetector;
import io.qameta.allure.detect.WellKnownFileExtensionsUtils;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.size;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("MultipleStringLiterals")
public class DefaultResultsVisitor implements ResultsVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResultsVisitor.class);

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    // so far maximum offset is 512 for supported files
    private static final int MAGIC_HEADER_LENGTH = 1024;

    private static final String UNKNOWN_PARAMETER_VALUE = "#___unknown_value___#";

    private static final Comparator<Parameter> PARAMETER_COMPARATOR = Comparator
            .comparing(Parameter::getName)
            .thenComparing(DefaultResultsVisitor::getParameterValue);

    private final Configuration configuration;

    private final Map<Path, Attachment> attachments;

    private final Set<TestResult> results;

    private final Map<String, Object> extra;

    public DefaultResultsVisitor(final Configuration configuration) {
        this.configuration = configuration;
        this.results = ConcurrentHashMap.newKeySet();
        this.attachments = new ConcurrentHashMap<>();
        this.extra = new ConcurrentHashMap<>();
    }

    @Override
    public Attachment visitAttachmentFile(final Path attachmentFile) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        return attachments.computeIfAbsent(attachmentFile, file -> {
            final String uid = context.getValue().get();
            final String realType = probeContentType(file);
            final String extension = Optional.of(getExtension(file.toString()))
                    .filter(s -> !s.isEmpty())
                    .map(s -> "." + s)
                    .orElseGet(() -> WellKnownFileExtensionsUtils.getExtensionByMimeType(realType));
            final String source = uid + (extension.isEmpty() ? "" : extension);
            final Long size = getFileSizeSafe(file);
            return new Attachment()
                    .setUid(uid)
                    .setName(file.getFileName().toString())
                    .setSource(source)
                    .setType(realType)
                    .setSize(size);
        });
    }

    @Override
    public void visitTestResult(final TestResult result) {
        final String testCaseHash = Optional.ofNullable(result.getTestCaseHash())
                .orElseGet(() -> getTestCaseHash(result));
        final String parametersHash = Optional.ofNullable(result.getParametersHash())
                .orElseGet(() -> getParametersHash(result));
        results.add(
                result
                        .setTestCaseHash(testCaseHash)
                        .setParametersHash(parametersHash)
        );
    }

    private static String getTestCaseHash(final TestResult result) {
        final String fullName = result.getFullName();
        return fullName == null || fullName.isEmpty()
                ? null
                : DigestUtils.md5Hex(fullName);
    }

    private static String getParametersHash(final TestResult result) {
        final List<Parameter> parameters = Optional.ofNullable(result.getParameters())
                .orElseGet(Collections::emptyList);
        final String value = parameters.stream()
                .filter(Objects::nonNull)
                .filter(parameter -> parameter.getName() != null && !parameter.getName().isEmpty())
                .sorted(PARAMETER_COMPARATOR)
                .map(parameter -> parameter.getName() + ":" + getParameterValue(parameter))
                .collect(Collectors.joining(","));
        return DigestUtils.md5Hex(value);
    }

    private static String getParameterValue(final Parameter parameter) {
        return Objects.toString(parameter.getValue(), UNKNOWN_PARAMETER_VALUE);
    }

    @Override
    public void visitExtra(final String name, final Object object) {
        extra.put(name, object);
    }

    @Override
    public void error(final String message, final Exception e) {
        //not implemented yet
    }

    @Override
    public void error(final String message) {
        //not implemented yet
    }

    public LaunchResults getLaunchResults() {
        return new DefaultLaunchResults(
                Collections.unmodifiableSet(results),
                Collections.unmodifiableMap(attachments),
                Collections.unmodifiableMap(extra)
        );
    }

    public static String probeContentType(final Path path) {
        try (InputStream stream = newInputStream(path)) {
            return Optional.ofNullable(probeContentType(stream, Objects.toString(path.getFileName())))
                    .orElse(APPLICATION_OCTET_STREAM);
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the media type of attachment {}", path, e);
            return APPLICATION_OCTET_STREAM;
        }
    }

    private static String probeContentType(final InputStream is, final String name) throws IOException {
        // first try to detect using name if provided
        final String lookup = WellKnownFileExtensionsUtils.lookup(name);
        if (Objects.nonNull(lookup)) {
            return lookup;
        }

        // try to read file header bytes and detect using magic bytes detector
        try (InputStream stream = new BufferedInputStream(is)) {
            final byte[] buffer = new byte[MAGIC_HEADER_LENGTH];
            final int bytesRead = stream.read(buffer);
            if (bytesRead <= 0) {
                return APPLICATION_OCTET_STREAM;
            }
            return MagicBytesContentTypeDetector.detectContentType(buffer);
        }
    }

    private static Long getFileSizeSafe(final Path path) {
        try {
            return size(path);
        } catch (IOException e) {
            LOGGER.warn("Could not get the size of file {}", path, e);
            return null;
        }
    }
}
