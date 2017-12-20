package io.qameta.allure.packages;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.Layer;
import io.qameta.allure.tree.TestResultTree;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;

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
        /* default */ TestResultTree getData(final List<LaunchResults> launchResults) {

        final TestResultTree packages = new TestResultTree(
                "packages",
                this::groupByPackages
        );

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .sorted(comparingByTimeAsc())
                .forEach(packages::add);

        return packages;
    }

    protected List<Layer> groupByPackages(final TestResult testResult) {
        return testResult.findOneLabel(LabelName.PACKAGE)
                .map(packageName -> Arrays.asList(packageName.split("\\.")))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(value -> new Layer("package", Collections.singletonList(value)))
                .collect(Collectors.toList());
    }
}

