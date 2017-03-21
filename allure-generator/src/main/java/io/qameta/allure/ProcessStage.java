package io.qameta.allure;

import io.qameta.allure.core.ProcessResultStage;
import io.qameta.allure.core.ProcessTestCaseStage;
import io.qameta.allure.core.ProcessTestRunStage;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;
import io.qameta.allure.writer.ReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStage.class);

    protected final AttachmentsStorage storage;

    protected final Map<String, Set<String>> filesNamesMap;

    protected final Map<String, Set<String>> widgetsNamesMap;

    protected final Map<String, Finalizer> finalizers;

    protected final Set<ResultsReader> testCaseReaders;

    protected final ProcessTestRunStage testRunStage;

    protected final ProcessTestCaseStage testCaseStage;

    protected final ProcessResultStage resultStage;

    protected final Map<String, TestCase> testCases = new HashMap<>();

    @Inject
    public ProcessStage(final AttachmentsStorage storage,
                        final @Named("report-data-folder") Map<String, Set<String>> filesNamesMap,
                        final @Named("report-widgets") Map<String, Set<String>> widgetsNamesMap,
                        final Map<String, Finalizer> finalizers,
                        final Set<ResultsReader> testCaseReaders,
                        final ProcessTestRunStage testRunStage,
                        final ProcessTestCaseStage testCaseStage,
                        final ProcessResultStage resultStage) {
        this.storage = storage;
        this.filesNamesMap = filesNamesMap;
        this.widgetsNamesMap = widgetsNamesMap;
        this.finalizers = finalizers;
        this.testCaseReaders = testCaseReaders;
        this.testRunStage = testRunStage;
        this.testCaseStage = testCaseStage;
        this.resultStage = resultStage;
    }

    @SuppressWarnings("PMD.UnnecessaryLocalBeforeReturn")
    public Statistic run(final ReportWriter writer, final Path... sources) {
        LOGGER.debug("Found {} results readers", testCaseReaders.size());
        final Map<String, Object> data = new HashMap<>();
        final Statistic statistic = processData(writer, data, sources);
        writeData(writer, data);
        writeAttachments(writer);
        return statistic;
    }

    private void writeAttachments(final ReportWriter writer) {
        storage.getAttachments().forEach((path, attachment) -> writeAttachment(writer, path, attachment));
    }

    private void writeAttachment(final ReportWriter writer,
                                 final Path attachmentFile,
                                 final Attachment attachment) {
        try (InputStream is = Files.newInputStream(attachmentFile)) {
            writer.writeAttachment(is, attachment);
        } catch (IOException e) {
            LOGGER.error("Could not read attachment file {} {} {}", attachment.getName(), attachmentFile, e);
        }
    }

    private Statistic processData(final ReportWriter writer,
                                  final Map<String, Object> data,
                                  final Path... sources) {
        final Statistic statistic = new Statistic();
        for (Path source : sources) {
            final TestRun testRun = testRunStage.read().apply(source);
            testRunStage.process(testRun).accept(data);

            final List<TestCaseResult> testCaseResults = readTestCases(source);
            LOGGER.debug("Found {} results for source {}", testCaseResults.size(), source.getFileName());
            for (TestCaseResult result : testCaseResults) {
                statistic.update(result);
                final String testCaseId = Objects.isNull(result.getTestCaseId())
                        ? UUID.randomUUID().toString()
                        : result.getTestCaseId();
                if (!testCases.containsKey(testCaseId)) {
                    TestCase testCase = createTestCase(result);
                    testCase.setId(testCaseId);
                    testCases.put(testCaseId, testCase);
                    testCaseStage.process(testRun, testCase).accept(data);
                }
                final TestCase testCase = testCases.get(testCaseId);
                testCase.updateLinks(result.getLinks());
                testCase.updateParametersNames(result.getParameters());
                resultStage.process(testRun, testCase, result).accept(data);
                writer.writeTestCase(result);
            }
        }
        return statistic;
    }

    @SuppressWarnings("unchecked")
    private void writeData(final ReportWriter writer, final Map<String, Object> data) {
        final Map<String, Object> widgets = new HashMap<>();
        data.forEach((uid, object) -> {
            final Set<String> fileNames = filesNamesMap.getOrDefault(uid, Collections.emptySet());
            fileNames.forEach(fileName -> {
                Finalizer finalizer = finalizers.getOrDefault(fileName, Finalizer.identity());
                writer.writeJsonData(fileName, finalizer.convert(object));
            });

            final Set<String> widgetNames = widgetsNamesMap.getOrDefault(uid, Collections.emptySet());
            widgetNames.forEach(name -> {
                Finalizer finalizer = finalizers.getOrDefault(name, Finalizer.identity());
                widgets.put(name, finalizer.convert(object));
            });
        });
        writer.writeJsonData("widgets.json", widgets);
    }

    private TestCase createTestCase(final TestCaseResult result) {
        return new TestCase()
                .withId(result.getTestCaseId())
                .withName(result.getName())
                .withDescription(result.getDescription())
                .withDescriptionHtml(result.getDescriptionHtml());
    }

    private List<TestCaseResult> readTestCases(final Path source) {
        return testCaseReaders.stream()
                .flatMap(reader -> reader.readResults(source).stream())
                .collect(Collectors.toList());
    }
}
