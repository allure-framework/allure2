package org.allurefw.report;

import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;
import org.allurefw.report.writer.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
    protected Map<String, Processor> processors;

    @Inject
    protected Map<String, Aggregator> aggregators;

    @Inject
    protected Writer writer;

    @Inject
    @DataNamesMap
    protected Map<String, Set<String>> filesNamesMap;

    @Inject
    @WidgetsNamesMap
    protected Map<String, Set<String>> widgetsNamesMap;

    @Inject
    protected Map<String, Finalizer> finalizers;

    @Inject
    protected Set<TestCaseResultsReader> testCaseReaders;

    @Inject
    protected TestRunReader testRunReader;

    @Inject
    protected Set<TestRunDetailsReader> testRunDetailsReaders;

    protected HashMap<String, TestCase> testCases = new HashMap<>();

    public void run(Path output, Path... sources) {
        LOGGER.debug("Process stage started...");

        Path dataDir = output.resolve("data");
        Path testCasesDir = dataDir.resolve("test-cases");

        Map<String, Object> data = new HashMap<>();
        for (Path source : sources) {
            TestRun testRun = testRunReader.readTestRun(source);
            testRunDetailsReaders.forEach(reader -> reader.readDetails(source).accept(testRun));
            List<TestCaseResult> testCaseResults = readTestCases(source);
            for (TestCaseResult result : testCaseResults) {
                TestCase testCase = getTestCase(result);
                testCase.updateLinks(result.getLinks());
                testCase.updateParametersNames(result.getParameters());

                LOGGER.debug("Processing test case: \"{}\"", result.getName());
                processors.forEach((uid, processor) -> processor.process(testRun, testCase, result));
                writer.write(testCasesDir, result.getSource(), result);

                aggregators.forEach((uid, aggregator) -> {
                    Object value = data.computeIfAbsent(uid, key -> aggregator.supplier(testRun).get());
                    //noinspection unchecked
                    aggregator.aggregate(testRun, testCase, result).accept(value);
                });
            }
        }

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
        Path attachmentsDir = dataDir.resolve("attachments");
        storage.getAttachments().forEach((path, attachment) ->
                writer.write(attachmentsDir, attachment.getSource(), path)
        );
    }

    private TestCase getTestCase(TestCaseResult result) {
        return testCases.computeIfAbsent(result.getId(), id -> new TestCase()
                .withId(id)
                .withName(result.getName())
                .withDescription(result.getDescription())
                .withDescriptionHtml(result.getDescriptionHtml())
        );
    }

    private List<TestCaseResult> readTestCases(Path source) {
        return testCaseReaders.stream()
                .flatMap(reader -> reader.readResults(source).stream())
                .collect(Collectors.toList());
    }
}
