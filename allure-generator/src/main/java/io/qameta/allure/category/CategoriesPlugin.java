package io.qameta.allure.category;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.Classifier;
import io.qameta.allure.tree.DefaultTree;
import io.qameta.allure.tree.TestResultClassifier;
import io.qameta.allure.tree.TestResultTreeLeaf;
import io.qameta.allure.tree.Tree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Plugin that generates data for Categories tab.
 *
 * @since 2.0
 */
public class CategoriesPlugin implements Aggregator, Reader {

    public static final String CATEGORIES_BLOCK_NAME = "categories";

    public static final Category FAILED_TESTS = new Category().withName("Product defects");

    public static final Category BROKEN_TESTS = new Category().withName("Test defects");

    private static final String CATEGORIES_FILE_NAME = "categories.json";

    //@formatter:off
    private static final TypeReference<List<Category>> CATEGORIES_TYPE =
        new TypeReference<List<Category>>() {};
    //@formatter:on

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path categoriesFile = directory.resolve(CATEGORIES_FILE_NAME);
        if (Files.exists(categoriesFile)) {
            try (InputStream is = Files.newInputStream(categoriesFile)) {
                final List<Category> categories = context.getValue().readValue(is, CATEGORIES_TYPE);
                visitor.visitExtra(CATEGORIES_BLOCK_NAME, categories);
            } catch (IOException e) {
                visitor.error("Could not read categories file " + categoriesFile, e);
            }
        }
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {

        addCategoriesForResults(launchesResults);

        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve("categories.json");
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, getData(launchesResults));
        }
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ void addCategoriesForResults(final List<LaunchResults> launchesResults) {
        launchesResults.forEach(launch -> {
            final List<Category> categories = launch.getExtra(CATEGORIES_BLOCK_NAME, Collections::emptyList);
            launch.getResults().forEach(result -> {
                final List<Category> resultCategories = result.getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<>());
                categories.forEach(category -> {
                    if (matches(result, category)) {
                        resultCategories.add(category);
                    }
                });
                if (resultCategories.isEmpty() && Status.FAILED.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<Category>()).add(FAILED_TESTS);
                }
                if (resultCategories.isEmpty() && Status.BROKEN.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<Category>()).add(BROKEN_TESTS);
                }
            });
        });
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ Tree<TestResult> getData(final List<LaunchResults> launchResults) {

        // @formatter:off
        final Tree<TestResult> xunit = new DefaultTree<>(
            "suites",
            this::groupByCategories,
            this::createLeaf
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(xunit::add);
        return xunit;
    }

    protected List<Classifier<TestResult>> groupByCategories(final TestResult testResult) {
        final Stream<TestResultClassifier> categories = testResult
                .<List<Category>>getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<>())
                .stream()
                .map(Category::getName)
                .map(TestResultClassifier::new);
        final Stream<TestResultClassifier> message = Stream.of(testResult.getStatusMessage().orElse("Without message"))
                .map(TestResultClassifier::new);

        return Stream.concat(categories, message)
                .collect(Collectors.toList());
    }

    protected Optional<TestResultTreeLeaf> createLeaf(final TestResult testResult) {
        if (testResult.<List<Category>>getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<>()).isEmpty()) {
            return Optional.empty();
        } else {
            return TestResultTreeLeaf.create(testResult);
        }
    }

    public static boolean matches(final TestResult result, final Category category) {
        boolean matchesStatus = category.getMatchedStatuses().isEmpty()
                || nonNull(result.getStatus())
                && category.getMatchedStatuses().contains(result.getStatus());
        boolean matchesMessage = isNull(category.getMessageRegex())
                || nonNull(result.getStatusDetails())
                && nonNull(result.getStatusDetails().getMessage())
                && matches(result.getStatusDetails().getMessage(), category.getMessageRegex());
        boolean matchesTrace = isNull(category.getTraceRegex())
                || nonNull(result.getStatusDetails())
                && nonNull(result.getStatusDetails().getTrace())
                && matches(result.getStatusDetails().getTrace(), category.getTraceRegex());
        boolean matchesFlaky = nonNull(result.getStatusDetails())
                && result.getStatusDetails().isFlaky() == category.isFlaky();
        return matchesStatus && matchesMessage && matchesTrace && matchesFlaky;
    }

    private static boolean matches(final String message, final String pattern) {
        return Pattern.compile(pattern, Pattern.DOTALL).matcher(message).matches();
    }
}
