package io.qameta.allure;

import io.qameta.allure.allure1.Allure1Reader;
import io.qameta.allure.attachment.AllureAttachmentsReader;
import io.qameta.allure.config.ReportConfig;
import io.qameta.allure.core.ReportWebPlugin;
import io.qameta.allure.entity.Executor;
import io.qameta.allure.entity.Job;
import io.qameta.allure.entity.Project;
import io.qameta.allure.junit.JunitReader;
import io.qameta.allure.trx.TrxReader;
import io.qameta.allure.xctest.XcTestReader;
import io.qameta.allure.xunit.XunitReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportGenerator {

    public void generate(final Path outputDirectory,
                         final List<Path> resultsDirectories) throws IOException, InterruptedException {
        generate(outputDirectory, resultsDirectories.toArray(new Path[resultsDirectories.size()]));
    }

    public void generate(final Path outputDirectory,
                         final Path... resultsDirectories) throws IOException, InterruptedException {
        final ReportConfig config = new ReportConfig()
                .setVersion("1.0")
                .setProjectName("some report")
                .setCategories(Collections.emptyList())
                .setEnvironmentVariables(Collections.singletonList("browser"))
                .setCustomFields(Arrays.asList("epic", "feature", "story", "suite"))
                .setTags(Collections.singletonList("tag"));

        final Project project = new Project().setName(config.getProjectName());
        final Job job = new Job()
                .setName("Some jenkins job")
                .setType("jenkins")
                .setUrl("https://example.org/jenkins");
        final Executor executor = new Executor().setName("Jenkins").setClassifier("jenkins");

        final DefaultReportContext context = new DefaultReportContext(project, executor, job);
        final TestResultNotifier notifier = new TestResultNotifier(Collections.emptyList());
        final DefaultTestResultService resultService = new DefaultTestResultService(config, notifier);
        final DefaultResultsVisitor visitor = new DefaultResultsVisitor(resultService);
        final CompositeResultsReader reader = new CompositeResultsReader(Arrays.asList(
                new Allure1Reader(),
                new AllureAttachmentsReader(),
                new JunitReader(),
                new TrxReader(),
                new XcTestReader(),
                new XunitReader()
        ));
        final DefaultPluginRegistry registry = new DefaultPluginRegistry();
        registry.addAggregator(new ReportWebPlugin());

        final CompositeAggregator aggregator = new CompositeAggregator(registry.getAggregators());

        final DirectoryWatcher watcher = new DirectoryWatcher();
        watcher.watch(file -> reader.readResultFile(visitor, file), resultsDirectories);
        watcher.stop();
        watcher.waitCompletion();

        aggregator.aggregate(context, resultService, outputDirectory);
    }
}
