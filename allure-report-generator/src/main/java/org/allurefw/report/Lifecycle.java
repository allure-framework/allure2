package org.allurefw.report;

import com.google.inject.Inject;
import org.allurefw.report.entity.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
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
    protected Set<TestCaseProcessor> processors;

    @Inject
    protected Set<TestCasePreparer> preparers;

    @Inject
    protected Set<ReportDataProvider> datas;

    @Inject
    protected Set<WidgetDataProvider> widgets;

    @Inject
    protected ReportConfig config;

    @Inject
    @ResultDirectories
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
        for (TestCase testCase : manager.getTestCases()) {
            findAnyResults = true;

            preparers.forEach(preparer -> preparer.prepare(testCase));
            //TODO don't forget to copy test case
            processors.forEach(processor -> processor.process(testCase));

            writer.write(testCasesDir, testCase.getSource(), testCase);
        }

        if (!findAnyResults && config.isFailIfNoResultsFound()) {
            throw new ReportGenerationException("Could not find any results");
        }

        //write data
        datas.forEach(provider -> writer.write(output, provider.getFileName(), provider.provide()));

        Path attachmentsDir = output.resolve("attachments");
        manager.getAttachments().forEach((path, attachment) ->
                writer.write(attachmentsDir, attachment.getSource(), path));
    }
}
