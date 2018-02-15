package io.qameta.allure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.qameta.allure.allure1.Allure1Reader;
import io.qameta.allure.attachment.AllureAttachmentsReader;
import io.qameta.allure.config.ReportConfig;
import io.qameta.allure.core.ReportWebPlugin;
import io.qameta.allure.core.ResultAggregator;
import io.qameta.allure.core.ResultExecutionAggregator;
import io.qameta.allure.entity.Executor;
import io.qameta.allure.entity.Job;
import io.qameta.allure.entity.Project;
import io.qameta.allure.junit.JunitReader;
import io.qameta.allure.trx.TrxReader;
import io.qameta.allure.xctest.XcTestReader;
import io.qameta.allure.xunit.XunitReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);

    public void generate(final Path outputDirectory,
                         final List<Path> resultsDirectories) throws IOException, InterruptedException {
        generate(outputDirectory, resultsDirectories.toArray(new Path[resultsDirectories.size()]));
    }

    public void generate(final Path outputDirectory,
                         final Path... resultsDirectories) throws IOException, InterruptedException {
        final ReportConfig config = readConfig();
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
        config.getGroups().forEach((id, group) -> registry.addAggregator(new DefaultTreeAggregator(id, group)));
        registry.addAggregator(new ResultAggregator());
        registry.addAggregator(new ResultExecutionAggregator());
        registry.addAggregator(new ReportWebPlugin());

        final CompositeAggregator aggregator = new CompositeAggregator(registry.getAggregators());

        final DirectoryWatcher watcher = new DirectoryWatcher();
        final Consumer<List<Path>> filesConsumer = files -> files.forEach(file -> reader.readResultFile(visitor, file));
        watcher.watch(filesConsumer, resultsDirectories);
        watcher.shutdown();
        watcher.awaitTermination(1, TimeUnit.MINUTES);
        watcher.shutdownNow();

        aggregator.aggregate(context, resultService, outputDirectory);
    }

    protected ReportConfig readConfig() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sample-config.yml")) {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(is, ReportConfig.class);
        }
    }
}
