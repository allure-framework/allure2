package io.qameta.allure;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.io.Files.getFileExtension;
import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.ReportApiUtils.getFileSizeSafe;
import static io.qameta.allure.ReportApiUtils.probeContentType;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultResultsVisitor implements ResultsVisitor {

    private final Map<Path, Attachment> attachments;

    private final Set<TestCase> testCases;

    private final Set<TestCaseResult> results;

    private final Map<String, String> configs;

    private final Map<String, Object> extra;

    public DefaultResultsVisitor() {
        this.attachments = new HashMap<>();
        this.testCases = new HashSet<>();
        this.results = new HashSet<>();
        this.configs = new HashMap<>();
        this.extra = new HashMap<>();
    }

    @Override
    public Attachment visitAttachmentFile(final Path attachmentFile) {
        return attachments.computeIfAbsent(attachmentFile, file -> {
            final String uid = generateUid();
            final String realType = probeContentType(file);
            final String extension = Optional.of(getFileExtension(file.toString()))
                    .filter(s -> !s.isEmpty())
                    .map(s -> "." + s)
                    .orElseGet(() -> ReportApiUtils.getExtensionByMimeType(realType));
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
    public void visitTestCase(TestCase testCase) {
        testCases.add(testCase);
    }

    @Override
    public void visitTestResult(TestCaseResult result) {
        results.add(result);
    }

    @Override
    public void visitConfiguration(Map<String, String> properties) {
        configs.putAll(properties);
    }

    @Override
    public void visitExtra(String name, Object object) {
        extra.put(name, object);
    }

    @Override
    public void error(String message, Exception e) {
    }

    @Override
    public void error(String message) {

    }

    @Override
    public LaunchResults getLaunchResults() {
        return new DefaultLaunchResults(
                Collections.unmodifiableSet(results),
                Collections.unmodifiableSet(testCases),
                Collections.unmodifiableMap(attachments),
                Collections.unmodifiableMap(extra)
        );
    }
}
