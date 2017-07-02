package io.qameta.allure.timeline;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree2.DefaultTree;
import io.qameta.allure.tree2.TestResultTreeLeaf;
import io.qameta.allure.tree2.Tree;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static io.qameta.allure.tree2.TreeUtils.groupByLabels;

/**
 * Plugin that generates data for Timeline tab.
 *
 * @since 2.0
 */
public class TimelinePlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {

        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        final RandomUidContext uidContext = configuration.requireContext(RandomUidContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve("timeline.json");
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, getData(uidContext.getValue(), launchesResults));
        }

    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ Tree<TestResult> getData(final Supplier<String> uidGenerator,
                                           final List<LaunchResults> launchResults) {

        // @formatter:off
        final Tree<TestResult> timeline = new DefaultTree<>(
            "timeline",
            testResult -> groupByLabels(uidGenerator, testResult, LabelName.HOST, LabelName.THREAD),
            TestResultTreeLeaf::create
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(timeline::add);
        return timeline;
    }
}
