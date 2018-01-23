package io.qameta.allure.category;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.AbstractCsvExportAggregator;
import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestStatus;
import io.qameta.allure.tree.Layer;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.Statistic.comparator;
import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Plugin that generates data for Categories tab.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class CategoriesPlugin extends CompositeAggregator implements ResultsReader {

    public static final String CATEGORIES = "categories";

    public static final Category FAILED_TESTS = new Category().setName("Product defects");

    public static final Category BROKEN_TESTS = new Category().setName("Test defects");

    public static final String JSON_FILE_NAME = "categories.json";

    public static final String CSV_FILE_NAME = "categories.csv";

    //@formatter:off
    private static final TypeReference<List<Category>> CATEGORIES_TYPE =
        new TypeReference<List<Category>>() {};
    //@formatter:on

    public CategoriesPlugin() {
        super(Arrays.asList(
                new JsonAggregator(), new CsvExportAggregator(), new WidgetAggregator()
        ));
    }

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path categoriesFile = directory.resolve(JSON_FILE_NAME);
        if (Files.exists(categoriesFile)) {
            try (InputStream is = Files.newInputStream(categoriesFile)) {
                final List<Category> categories = context.getValue().readValue(is, CATEGORIES_TYPE);
                visitor.visitExtra(CATEGORIES, categories);
            } catch (IOException e) {
                visitor.error("Could not read categories file " + categoriesFile, e);
            }
        }
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static TestResultTree getData(final List<LaunchResults> launchResults) {

        // @formatter:off
        final TestResultTree categories = new TestResultTree(CATEGORIES, CategoriesPlugin::groupByCategories);
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .sorted(comparingByTimeAsc())
                .forEach(categories::add);
        return categories;
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static void addCategoriesForResults(final List<LaunchResults> launchesResults) {
        launchesResults.forEach(launch -> {
            final List<Category> categories = launch.getExtra(CATEGORIES, Collections::emptyList);
            launch.getResults().forEach(result -> {
                final List<Category> resultCategories = result.getExtraBlock(CATEGORIES, new ArrayList<>());
                categories.forEach(category -> {
                    if (matches(result, category)) {
                        resultCategories.add(category);
                    }
                });
                if (resultCategories.isEmpty() && TestStatus.FAILED.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(FAILED_TESTS);
                }
                if (resultCategories.isEmpty() && TestStatus.BROKEN.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(BROKEN_TESTS);
                }
            });
        });
    }

    protected static List<Layer> groupByCategories(final TestResult testResult) {
        final Set<String> categories = testResult
                .<List<Category>>getExtraBlock(CATEGORIES, new ArrayList<>())
                .stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        final Layer categoriesLayer = new Layer("category", categories);
        final Layer messageLayer = new Layer("message", Collections.singletonList(testResult.getMessage()));
        return Arrays.asList(categoriesLayer, messageLayer);
    }

    public static boolean matches(final TestResult result, final Category category) {
        boolean matchesStatus = category.getMatchedStatuses().isEmpty()
                || nonNull(result.getStatus())
                && category.getMatchedStatuses().contains(result.getStatus());
        boolean matchesMessage = isNull(category.getMessageRegex())
                || nonNull(result.getMessage())
                && matches(result.getMessage(), category.getMessageRegex());
        boolean matchesTrace = isNull(category.getTraceRegex())
                || nonNull(result.getTrace())
                && matches(result.getTrace(), category.getTraceRegex());
        boolean matchesFlaky = result.isFlaky() == category.isFlaky();
        return matchesStatus && matchesMessage && matchesTrace && matchesFlaky;
    }

    private static boolean matches(final String message, final String pattern) {
        return Pattern.compile(pattern, Pattern.DOTALL).matcher(message).matches();
    }

    @Override
    public void aggregate(final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        addCategoriesForResults(launchesResults);
        super.aggregate(launchesResults, outputDirectory);
    }

    private static class JsonAggregator extends AbstractJsonAggregator {

        JsonAggregator() {
            super(JSON_FILE_NAME);
        }

        @Override
        protected TestResultTree getData(final List<LaunchResults> launches) {
            return CategoriesPlugin.getData(launches);
        }
    }

    private static class CsvExportAggregator extends AbstractCsvExportAggregator<CsvExportCategory> {

        CsvExportAggregator() {
            super(CSV_FILE_NAME, CsvExportCategory.class);
        }

        @Override
        protected List<CsvExportCategory> getData(final List<LaunchResults> launchesResults) {
            final List<CsvExportCategory> exportLabels = new ArrayList<>();
            final TestResultTree data = CategoriesPlugin.getData(launchesResults);
            final List<TreeWidgetItem> items = data.getGroups().stream()
                    .map(TreeWidgetItem::create)
                    .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                    .collect(Collectors.toList());
            items.forEach(item -> exportLabels.add(new CsvExportCategory(item)));
            return exportLabels;
        }
    }

    private static class WidgetAggregator extends AbstractJsonAggregator {

        WidgetAggregator() {
            super("widgets", JSON_FILE_NAME);
        }

        @Override
        protected Object getData(final List<LaunchResults> launches) {
            final TestResultTree data = CategoriesPlugin.getData(launches);
            final List<TreeWidgetItem> items = data.getGroups().stream()
                    .map(TreeWidgetItem::create)
                    .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            return new TreeWidgetData().setItems(items).setTotal(data.getGroups().size());
        }
    }
}
