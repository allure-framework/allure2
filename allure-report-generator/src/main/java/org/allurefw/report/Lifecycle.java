package org.allurefw.report;

import com.google.inject.Inject;
import org.allurefw.report.entity.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class Lifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lifecycle.class);

    @Inject
    protected Set<ResultsProcessor> results;

    @Inject
    protected Set<TestCasePreparer> preparers;

    @Inject
    protected Map<Object, Aggregator> aggregators;

    @Inject
    protected Set<DataCollector> collectors;

    @Inject
    @ReportData
    protected Map<String, Object> reportData;

    @Inject
    protected ReportConfig config;

    @Inject
    @ResultsDirectories
    protected Path[] resultsDirectories;

    @Inject
    protected DefaultReportDataManager manager;

    @Inject
    protected Writer writer;

    public void generate(Path output) {
        //read the data
        for (ResultsProcessor result : results) {
            result.setReportDataManager(manager);

            for (Path path : resultsDirectories) {
                result.process(path);
            }
        }

        //process the data
        Path testCasesDir = output.resolve("test-cases");

        boolean findAnyResults = false;


        Map<DataCollector, Object> reportData = new HashMap<>();
        collectors.forEach(collector -> reportData.put(collector, collector.supplier().get()));

        for (TestCase testCase : manager.getTestCases()) {
            findAnyResults = true;

            preparers.forEach(preparer -> preparer.prepare(testCase));
            //TODO don't forget to copy test case

            //noinspection unchecked
            reportData.forEach((collector, identity)
                    -> collector.accumulator().accept(identity, testCase));

            //noinspection unchecked
            aggregators.forEach((identity, aggregator) -> aggregator.aggregate(identity, testCase));

            writer.write(testCasesDir, testCase.getSource(), testCase);
        }

        if (!findAnyResults && config.isFailIfNoResultsFound()) {
            throw new ReportGenerationException("Could not find any results");
        }

        //write data
//        reportData.forEach((fileName, data) -> writer.write(output, fileName, data));

        Path attachmentsDir = output.resolve("attachments");
        manager.getAttachments().forEach((path, attachment) ->
                writer.write(attachmentsDir, attachment.getSource(), path));
    }

}
