package io.qameta.allure.packages;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.Classifier;
import io.qameta.allure.tree.DefaultTree;
import io.qameta.allure.tree.TestResultClassifier;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.TestResultTreeLeaf;
import io.qameta.allure.tree.Tree;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The plugin adds packages tab to the report.
 *
 * @since 2.0
 */
public class PackagesPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve("packages.json");
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, getData(launchesResults));
        }
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ Tree<TestResult> getData(final List<LaunchResults> launchResults) {

        // @formatter:off
        final Tree<TestResult> packages = new DefaultTree<>(
            "packages",
            this::groupByPackages,
            this::createLeaf
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(packages::add);

        return collapseGroupsWithOnlyOneChild(packages);
    }

    protected List<Classifier<TestResult>> groupByPackages(final TestResult testResult) {
        return testResult.findOne(LabelName.PACKAGE)
                .map(packageName -> Arrays.asList(packageName.split("\\.")))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(TestResultClassifier::new)
                .collect(Collectors.toList());
    }

    protected Tree<TestResult> collapseGroupsWithOnlyOneChild(final Tree<TestResult> packages) {
        packages.getChildren().stream()
                .filter(TestResultTreeGroup.class::isInstance)
                .map(TestResultTreeGroup.class::cast)
                .forEach(this::collapseGroupsWithOnlyOneChild);
        return packages;
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    protected void collapseGroupsWithOnlyOneChild(final TestResultTreeGroup groupNode) {
        groupNode.getChildren().stream()
                .filter(TestResultTreeGroup.class::isInstance)
                .map(TestResultTreeGroup.class::cast)
                .forEach(this::collapseGroupsWithOnlyOneChild);

        final long count = groupNode.getChildren().stream()
                .filter(TestResultTreeGroup.class::isInstance)
                .count();

        if (count == 1) {
            groupNode.getChildren().stream()
                    .filter(TestResultTreeGroup.class::isInstance)
                    .map(TestResultTreeGroup.class::cast)
                    .forEach(next -> {
                        final String name = getName(groupNode, next);
                        groupNode.setName(name);
                        groupNode.setUid(name);
                        groupNode.setChildren(next.getChildren());
                    });
        }
    }

    protected String getName(final TestResultTreeGroup parent, final TestResultTreeGroup child) {
        return String.format("%s.%s", parent.getName(), child.getName());
    }

    private Optional<TestResultTreeLeaf> createLeaf(final TestResult testResult) {
        final String name = testResult
                .findOne(LabelName.TEST_METHOD)
                .filter(method -> !method.isEmpty())
                .orElseGet(testResult::getName);
        return Optional.of(new TestResultTreeLeaf(
                name,
                testResult.getUid(),
                testResult.getStatus(), testResult.getTime(),
                testResult.getStatusDetailsSafe().isFlaky(),
                testResult.getParameterValues()
        ));
    }
}
