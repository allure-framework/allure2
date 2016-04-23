package org.allurefw.report;

import com.google.inject.Inject;
import org.allurefw.report.entity.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class Lifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lifecycle.class);

    @Inject
    protected Set<ResultsProcessor> results;

    @Inject
    protected Map<String, Aggregator> aggregators;

    @Inject
    protected Map<String, Processor> processors;

    @Inject
    @ReportFilesNamesMap
    protected Map<String, String> filesNames;

    @Inject
    @WidgetsNamesMap
    protected Map<String, String> widgetsNames;

    @Inject
    @WidgetDataFinalizer
    protected Map<String, Finalizer> widgetDataFinalizers;

    @Inject
    protected ReportConfig config;

    @Inject
    @ResultsDirectories
    protected Path[] resultsDirectories;

    @Inject
    protected DefaultReportDataManager manager;

    @Inject
    protected Writer writer;

    @Inject
    @PluginNames
    protected Set<String> pluginNames;

    public void generate(Path output) {
        LOGGER.debug("Write index.html...");
        writer.writeIndexHtml(output, pluginNames);

        LOGGER.debug("Reading stage started...");
        for (ResultsProcessor result : results) {
            result.setReportDataManager(manager);

            for (Path path : resultsDirectories) {
                result.process(path);
            }
        }

        LOGGER.debug("Process stage started...");
        Path dataDir = output.resolve("data");
        Path testCasesDir = dataDir.resolve("test-cases");

        boolean findAnyResults = false;

        Map<String, Object> data = new HashMap<>();
        for (TestCase testCase : manager.getTestCases()) {
            findAnyResults = true;

            processors.forEach((uid, processor) -> processor.process(testCase));

            writer.write(testCasesDir, testCase.getSource(), testCase);
            aggregators.forEach((uid, aggregator) -> {
                Object value = data.computeIfAbsent(uid, key -> aggregator.supplier().get());
                //noinspection unchecked
                aggregator.accumulator().accept(value, testCase);
            });
        }

        if (!findAnyResults && config.isFailIfNoResultsFound()) {
            throw new ReportGenerationException("Could not find any results");
        }
        LOGGER.debug("Writing stage started...");

        Map<String, Object> widgets = new HashMap<>();
        data.forEach((uid, object) -> {
            if (filesNames.containsKey(uid)) {
                String fileName = filesNames.get(uid);
                writer.write(dataDir, fileName, object);
            }
            if (widgetsNames.containsKey(uid)) {
                String widgetName = widgetsNames.get(uid);
                Finalizer finalizer = widgetDataFinalizers.getOrDefault(uid, Finalizer.identity());
                //noinspection unchecked
                widgets.put(widgetName, finalizer.finalize(object));
            }
        });

        writer.write(dataDir, "widgets.json", widgets);

        Map<String, Object> report = new HashMap<>();
        report.put("name", "Sample Allure 2.0 Report");
        report.put("size", 1151131);
        report.put("time", 6014);
        writer.write(dataDir, "report.json", report);

        Path attachmentsDir = dataDir.resolve("attachments");
        manager.getAttachments().forEach((path, attachment) ->
                writer.write(attachmentsDir, attachment.getSource(), path)
        );
    }

}
