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
package io.qameta.allure;

import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.size;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.tika.mime.MimeTypes.getDefaultMimeTypes;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("MultipleStringLiterals")
public class DefaultResultsVisitor implements ResultsVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResultsVisitor.class);

    public static final String WILDCARD = "*/*";

    private final Configuration configuration;

    private final Map<Path, Attachment> attachments;

    private final Set<TestResult> results;

    private final Map<String, Object> extra;

    public DefaultResultsVisitor(final Configuration configuration) {
        this.configuration = configuration;
        this.results = new HashSet<>();
        this.attachments = new HashMap<>();
        this.extra = new HashMap<>();
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
                    .orElseGet(() -> getExtensionByMimeType(realType));
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
        results.add(result);
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

    private static String getExtensionByMimeType(final String type) {
        try {
            return getDefaultMimeTypes().forName(type).getExtension();
        } catch (Exception e) {
            LOGGER.warn("Can't detect extension for MIME-type {} {}", type, e);
            return "";
        }
    }

    public static String probeContentType(final Path path) {
        try (InputStream stream = newInputStream(path)) {
            return probeContentType(stream, Objects.toString(path.getFileName()));
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the media type of attachment {} {}", path, e);
            return WILDCARD;
        }
    }

    public static String probeContentType(final InputStream is, final String name) {
        try (InputStream stream = new BufferedInputStream(is)) {
            final Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, name);
            return getDefaultMimeTypes().detect(stream, metadata).toString();
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the media type of attachment {} {}", name, e);
            return WILDCARD;
        }
    }

    private static Long getFileSizeSafe(final Path path) {
        try {
            return size(path);
        } catch (IOException e) {
            LOGGER.warn("Could not get the size of file {} {}", path, e);
            return null;
        }
    }
}
