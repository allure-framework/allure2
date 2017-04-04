package io.qameta.allure;

import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import org.apache.tika.metadata.Metadata;
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
public class DefaultResultsVisitor implements ResultsVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResultsVisitor.class);

    private static final Metadata METADATA = new Metadata();

    private final Configuration configuration;

    private final Map<Path, Attachment> attachments;

    private final Set<TestCase> testCases;

    private final Set<TestCaseResult> results;

    private final Map<String, String> configs;

    private final Map<String, Object> extra;

    public DefaultResultsVisitor(final Configuration configuration) {
        this.configuration = configuration;
        this.attachments = new HashMap<>();
        this.testCases = new HashSet<>();
        this.results = new HashSet<>();
        this.configs = new HashMap<>();
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
                    .withUid(uid)
                    .withName(file.getFileName().toString())
                    .withSource(source)
                    .withType(realType)
                    .withSize(size);
        });
    }

    @Override
    public void visitTestCase(final TestCase testCase) {
        testCases.add(testCase);
    }

    @Override
    public void visitTestResult(final TestCaseResult result) {
        results.add(result);
    }

    @Override
    public void visitConfiguration(final Map<String, String> properties) {
        configs.putAll(properties);
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

    @Override
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

    private static String probeContentType(final Path path) {
        try (InputStream stream = newInputStream(path)) {
            return probeContentType(stream, Objects.toString(path.getFileName()));
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the mime-type of attachment {} {}", path, e);
            return "unknown";
        }
    }

    private static String probeContentType(final InputStream is, final String name) {
        try (InputStream stream = new BufferedInputStream(is)) {
            return getDefaultMimeTypes().detect(stream, METADATA).toString();
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the mime-type of attachment {} {}", name, e);
            return "unknown";
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
