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

    @Inject
    protected AttachmentsStorage storage;

    @Inject
    @Named("report-data-folder")
    protected Map<String, Set<String>> filesNamesMap;

    @Inject
    @Named("report-widgets")
    protected Map<String, Set<String>> widgetsNamesMap;

    @Inject
    protected Map<String, Finalizer> finalizers;

    @Inject
    protected Set<ResultsReader> testCaseReaders;

    @Inject
    protected ProcessTestRunStage testRunStage;

    @Inject
    protected ProcessTestCaseStage testCaseStage;

    @Inject
    protected ProcessResultStage resultStage;

    protected HashMap<String, TestCase> testCases = new HashMap<>();

    public Statistic run(ReportWriter writer, Path... sources) {
        LOGGER.debug("Found {} results readers", testCaseReaders.size());
        Map<String, Object> data = new HashMap<>();
        Statistic statistic = processData(writer, data, sources);
        writeData(writer, data);
        writeAttachments(writer);
        return statistic;
    }

    private void writeAttachments(ReportWriter writer) {
        storage.getAttachments().forEach((path, attachment) -> writeAttachment(writer, path, attachment));
    }

    private void writeAttachment(ReportWriter writer, Path attachmentFile, Attachment attachment) {
        try (InputStream is = Files.newInputStream(attachmentFile)) {
            writer.writeAttachment(is, attachment);
        } catch (IOException e) {
            LOGGER.error("Could not read attachment file {} {} {}", attachment.getName(), attachmentFile, e);
        }
    }

    private Statistic processData(ReportWriter writer, Map<String, Object> data, Path[] sources) {
        Statistic statistic = new Statistic();
        for (Path source : sources) {
            TestRun testRun = testRunStage.read().apply(source);
            testRunStage.process(testRun).accept(data);

            List<TestCaseResult> testCaseResults = readTestCases(source);
            LOGGER.debug("Found {} results for source {}", testCaseResults.size(), source.getFileName());
            for (TestCaseResult result : testCaseResults) {
                statistic.update(result);
                String testCaseId = Objects.isNull(result.getId()) ? UUID.randomUUID().toString() : result.getId();
                if (!testCases.containsKey(testCaseId)) {
                    TestCase testCase = createTestCase(result);
                    testCase.setId(testCaseId);
                    testCases.put(testCaseId, testCase);
                    testCaseStage.process(testRun, testCase).accept(data);
                }
                TestCase testCase = testCases.get(testCaseId);
                testCase.updateLinks(result.getLinks());
                testCase.updateParametersNames(result.getParameters());
                resultStage.process(testRun, testCase, result).accept(data);
                writer.writeTestCase(result);
            }
        }
        return statistic;
    }

    private void writeData(ReportWriter writer, Map<String, Object> data) {
        Map<String, Object> widgets = new HashMap<>();
        data.forEach((uid, object) -> {
            Set<String> fileNames = filesNamesMap.getOrDefault(uid, Collections.emptySet());
            fileNames.forEach(fileName -> {
                Finalizer finalizer = finalizers.getOrDefault(fileName, Finalizer.identity());
                //noinspection unchecked
                writer.writeJsonData(fileName, finalizer.convert(object));
            });

            Set<String> widgetNames = widgetsNamesMap.getOrDefault(uid, Collections.emptySet());
            widgetNames.forEach(name -> {
                Finalizer finalizer = finalizers.getOrDefault(name, Finalizer.identity());
                //noinspection unchecked
                widgets.put(name, finalizer.convert(object));
            });
        });
        writer.writeJsonData("widgets.json", widgets);
    }

    private TestCase createTestCase(TestCaseResult result) {
        return new TestCase()
                .withId(result.getId())
                .withName(result.getName())
                .withDescription(result.getDescription())
                .withDescriptionHtml(result.getDescriptionHtml());
    }

    private List<TestCaseResult> readTestCases(Path source) {
        return testCaseReaders.stream()
                .flatMap(reader -> reader.readResults(source).stream())
                .collect(Collectors.toList());
    }
}
