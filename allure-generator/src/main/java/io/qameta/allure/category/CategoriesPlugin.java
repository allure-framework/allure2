package io.qameta.allure.category;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.AbstractTreeAggregator;
import io.qameta.allure.tree.TreeGroup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Plugin that generates data for Categories tab.
 *
 * @since 2.0
 */
public class CategoriesPlugin extends AbstractTreeAggregator implements Reader {

    private static final Category FAILED_TESTS = new Category().withName("Product defects");

    private static final Category BROKEN_TESTS = new Category().withName("Test defects");

    private static final String CATEGORIES = "categories";

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
                visitor.visitExtra(CATEGORIES, categories);
            } catch (IOException e) {
                visitor.error("Could not read categories file " + categoriesFile, e);
            }
        }
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        launchesResults.forEach(launch -> {
            final List<Category> categories = launch.getExtra(CATEGORIES, Collections::emptyList);
            launch.getResults().forEach(result -> {
                final List<Category> resultCategories = result.getExtraBlock(CATEGORIES, new ArrayList<>());
                categories.forEach(category -> {
                    if (matches(result, category)) {
                        resultCategories.add(category);
                    }
                });
                if (resultCategories.isEmpty() && Status.FAILED.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(FAILED_TESTS);
                }
                if (resultCategories.isEmpty() && Status.BROKEN.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(BROKEN_TESTS);
                }
            });
        });

        super.aggregate(configuration, launchesResults, outputDirectory);
    }

    @Override
    protected boolean shouldProcess(final TestResult result) {
        return !result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).isEmpty();
    }

    @Override
    protected String getFileName() {
        return CATEGORIES_FILE_NAME;
    }

    @Override
    protected List<TreeGroup> getGroups(final TestResult result) {
        final List<Category> categories = result.getExtraBlock(CATEGORIES);
        final String message = result.getStatusMessage().orElse("Without message");
        return Arrays.asList(
                TreeGroup.values(categories.stream().map(Category::getName).toArray(String[]::new)),
                TreeGroup.values(message)
        );
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
        return matchesStatus && matchesMessage && matchesTrace;
    }

    private static boolean matches(final String message, final String pattern) {
        return Pattern.compile(pattern, Pattern.DOTALL).matcher(message).matches();
    }
}
