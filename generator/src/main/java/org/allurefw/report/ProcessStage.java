package org.allurefw.report;

import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.writer.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStage.class);

    @Inject
    protected Set<TestCaseResult> testCases;

    @Inject
    protected Map<Path, Attachment> attachments;

    @Inject
    protected ReportConfig config;

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

    public void run(Path output) {
        LOGGER.debug("Process stage started...");

        if (testCases.isEmpty()) {
            throw new ReportGenerationException("Could not find any results");
        }

        Path dataDir = output.resolve("data");
        Path testCasesDir = dataDir.resolve("test-cases");

        Map<String, Object> data = new HashMap<>();
        testCases.forEach(testCase -> {
            LOGGER.debug("Processing test case: \"{}\"", testCase.getName());
            processors.forEach((uid, processor) -> processor.process(testCase));
            writer.write(testCasesDir, testCase.getSource(), testCase);
            aggregators.forEach((uid, aggregator) -> {
                Object value = data.computeIfAbsent(uid, key -> aggregator.supplier().get());
                //noinspection unchecked
                aggregator.accumulator().accept(value, testCase);
            });
        });

        Map<String, Object> widgets = new HashMap<>();
        data.forEach((uid, object) -> {
            Set<String> fileNames = filesNamesMap.getOrDefault(uid, Collections.emptySet());
            fileNames.forEach(fileName -> {
                Finalizer finalizer = finalizers.getOrDefault(fileName, Finalizer.identity());
                //noinspection unchecked
                writer.write(dataDir, fileName, finalizer.finalize(object));
            });

            Set<String> widgetNames = widgetsNamesMap.getOrDefault(uid, Collections.emptySet());
            widgetNames.forEach(name -> {
                Finalizer finalizer = finalizers.getOrDefault(name, Finalizer.identity());
                //noinspection unchecked
                widgets.put(name, finalizer.finalize(object));
            });
        });

        writer.write(dataDir, "widgets.json", widgets);

        Map<String, Object> report = new HashMap<>();
        report.put("name", "Sample Allure 2.0 Report");
        report.put("size", 1151131);
        report.put("time", 6014);
        writer.write(dataDir, "report.json", report);

        Path attachmentsDir = dataDir.resolve("attachments");
        attachments.forEach((path, attachment) ->
                writer.write(attachmentsDir, attachment.getSource(), path)
        );


    }
}
