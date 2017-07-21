package io.qameta.allure.packages;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.DefaultTreeLayer;
import io.qameta.allure.tree.TestResultGroupFactory;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.TestResultTreeLeaf;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeLayer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

        final Tree<TestResult> packages = new TestResultTree(
                "packages",
                this::groupByPackages,
                new TestResultGroupFactory(),
                this::createLeaf
        );

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(packages::add);

        return collapseGroupsWithOnlyOneChild(packages);
    }

    protected List<TreeLayer> groupByPackages(final TestResult testResult) {
        return testResult.findOneLabel(LabelName.PACKAGE)
                .map(packageName -> Arrays.asList(packageName.split("\\.")))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(DefaultTreeLayer::new)
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

    private TestResultTreeLeaf createLeaf(final TestResultTreeGroup parent, final TestResult testResult) {
        final String name = testResult
                .findOneLabel(LabelName.TEST_METHOD)
                .filter(method -> !method.isEmpty())
                .orElseGet(testResult::getName);
        return new TestResultTreeLeaf(
                parent.getUid(),
                name,
                testResult.getUid(),
                testResult.getStatus(), testResult.getTime(),
                testResult.getStatusDetailsSafe().isFlaky(),
                testResult.getParameterValues()
        );
    }
}
