package org.allurefw.report;

import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;
import org.allurefw.report.writer.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStage.class);

    @Inject
    protected AttachmentsStorage storage;

    @Inject
    protected Map<String, Processor> resultProcessors;

    @Inject
    protected Map<String, TestRunAggregator> testRunAggregators;

    @Inject
    protected Map<String, TestCaseAggregator> testCaseAggregators;

    @Inject
    protected Map<String, ResultAggregator> resultAggregators;

    @Inject
    protected Writer writer;

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
    protected TestRunReader testRunReader;

    @Inject
    protected Set<TestRunDetailsReader> testRunDetailsReaders;

    protected HashMap<String, TestCase> testCases = new HashMap<>();

    public void run(Path output, Path... sources) {
        LOGGER.debug("Process stage started...");

        Path dataDirectory = output.resolve("data");
        Path testCasesDirectory = dataDirectory.resolve("test-cases");
        Path attachmentsDirectory = dataDirectory.resolve("attachments");

        Map<String, Object> data = new HashMap<>();
        processData(testCasesDirectory, data, sources);
        writeData(dataDirectory, data);
        writeAttachments(attachmentsDirectory);
    }

    private void writeAttachments(Path attachmentsDirectory) {
        storage.getAttachments().forEach((path, attachment) ->
                writer.write(attachmentsDirectory, attachment.getSource(), path)
        );
    }

    private void processData(Path testCasesDir, Map<String, Object> data, Path[] sources) {
        for (Path source : sources) {
            TestRun testRun = testRunReader.readTestRun(source);
            testRunDetailsReaders.forEach(reader -> reader.readDetails(source).accept(testRun));
            each(data, testRun);
            List<TestCaseResult> testCaseResults = readTestCases(source);
            for (TestCaseResult result : testCaseResults) {
                if (!testCases.containsKey(result.getId())) {
                    TestCase testCase = createTestCase(result);
                    testCases.put(result.getId(), testCase);
                    each(data, testRun, testCase);
                }
                TestCase testCase = testCases.get(result.getId());
                testCase.updateLinks(result.getLinks());
                testCase.updateParametersNames(result.getParameters());
                writer.write(testCasesDir, result.getSource(), result);
                each(data, testRun, testCase, result);
            }
        }
    }

    private void writeData(Path dataDir, Map<String, Object> data) {
        Map<String, Object> widgets = new HashMap<>();
        data.forEach((uid, object) -> {
            Set<String> fileNames = filesNamesMap.getOrDefault(uid, Collections.emptySet());
            fileNames.forEach(fileName -> {
                Finalizer finalizer = finalizers.getOrDefault(fileName, Finalizer.identity());
                //noinspection unchecked
                writer.write(dataDir, fileName, finalizer.convert(object));
            });

            Set<String> widgetNames = widgetsNamesMap.getOrDefault(uid, Collections.emptySet());
            widgetNames.forEach(name -> {
                Finalizer finalizer = finalizers.getOrDefault(name, Finalizer.identity());
                //noinspection unchecked
                widgets.put(name, finalizer.convert(object));
            });
        });
        writer.write(dataDir, "widgets.json", widgets);
    }

    private void each(Map<String, Object> data, TestRun testRun) {
        testRunAggregators.forEach((uid, aggregator) -> {
            Object value = data.computeIfAbsent(uid, key -> aggregator.supplier().get());
            //noinspection unchecked
            aggregator.aggregate(testRun).accept(value);
        });
    }

    private void each(Map<String, Object> data, TestRun testRun, TestCase testCase) {
        testCaseAggregators.forEach((uid, aggregator) -> {
            Object value = data.computeIfAbsent(uid, key -> aggregator.supplier(testRun).get());
            //noinspection unchecked
            aggregator.aggregate(testRun, testCase).accept(value);
        });
    }

    private void each(Map<String, Object> data, TestRun testRun, TestCase testCase, TestCaseResult result) {
        resultProcessors.forEach((uid, processor) -> processor.process(testRun, testCase, result));
        resultAggregators.forEach((uid, aggregator) -> {
            Object value = data.computeIfAbsent(uid, key -> aggregator.supplier(testRun, testCase).get());
            //noinspection unchecked
            aggregator.aggregate(testRun, testCase, result).accept(value);
        });
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
