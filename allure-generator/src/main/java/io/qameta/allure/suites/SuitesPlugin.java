package io.qameta.allure.suites;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree2.Classifier;
import io.qameta.allure.tree2.DefaultTree;
import io.qameta.allure.tree2.TestResultTreeGroup;
import io.qameta.allure.tree2.TestResultTreeLeaf;
import io.qameta.allure.tree2.Tree;
import io.qameta.allure.tree2.TreeGroup;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin that generates data for Suites tab.
 *
 * @since 2.0
 */
public class SuitesPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        final RandomUidContext uidContext = configuration.requireContext(RandomUidContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve("suites.json");
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, getData(uidContext.getValue(), launchesResults));
        }
    }

    /* default */ Tree<TestResult> getData(final Supplier<String> uidGenerator,
                                           final List<LaunchResults> launchResults) {
        final Tree<TestResult> xunit = new DefaultTree<>(
                "suites",
                testResult -> bySuites(uidGenerator, testResult),
                TestResultTreeLeaf::create
        );

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(xunit::add);
        return xunit;
    }

    protected List<Classifier<TestResult>> bySuites(final Supplier<String> uidGenerator,
                                                    final TestResult testResult) {
        return Stream.of(LabelName.PARENT_SUITE, LabelName.SUITE, LabelName.SUB_SUITE)
                .map(testResult::findAll)
                .filter(strings -> !strings.isEmpty())
                .map(names -> new Classifier<TestResult>() {
                    @Override
                    public List<String> classify(final TestResult item) {
                        return names;
                    }

                    @Override
                    public TreeGroup factory(final String name, final TestResult item) {
                        return new TestResultTreeGroup(name, uidGenerator.get());
                    }
                })
                .collect(Collectors.toList());
    }
}
