package io.qameta.allure.category;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Configuration;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreePlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


/**
 * @author charlie (Dmitry Baev).
 */
public class CategoryPlugin extends TreePlugin implements ResultsReader {

    private static final Category UNKNOWN_FAILURE = new Category().withName("Unknown failure");

    private static final Category UNKNOWN_ERROR = new Category().withName("Unknown error");

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
    public void process(final Configuration configuration,
                        final List<LaunchResults> launches,
                        final Path outputDirectory) throws IOException {
        launches.forEach(launch -> {
            final List<Category> categories = launch.getExtra(CATEGORIES, Collections::emptyList);
            launch.getResults().forEach(result -> {
                final List<Category> resultCategories = result.getExtraBlock(CATEGORIES, new ArrayList<>());
                categories.forEach(category -> {
                    if (matches(result, category)) {
                        resultCategories.add(category);
                    }
                });
                if (resultCategories.isEmpty() && Status.FAILED.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(UNKNOWN_FAILURE);
                }
                if (resultCategories.isEmpty() && Status.BROKEN.equals(result.getStatus())) {
                    result.getExtraBlock(CATEGORIES, new ArrayList<Category>()).add(UNKNOWN_ERROR);
                }
            });
        });

        super.process(configuration, launches, outputDirectory);
    }

    @Override
    protected String getFileName() {
        return CATEGORIES_FILE_NAME;
    }

    @Override
    protected List<TreeGroup> getGroups(final TestCaseResult result) {
        final List<Category> categories = result.getExtraBlock(CATEGORIES);
        final String message = result.getStatusMessage().orElse("Empty message");
        return Arrays.asList(
                TreeGroup.values(categories.stream().map(Category::getName).toArray(String[]::new)),
                TreeGroup.values(message)
        );
    }

    public static boolean matches(final TestCaseResult result, final Category category) {
        boolean matchesStatus = category.getMatchedStatuses().isEmpty()
                || nonNull(result.getStatus())
                && category.getMatchedStatuses().contains(result.getStatus());
        boolean matchesMessage = isNull(category.getMessageRegex())
                || nonNull(result.getStatusDetails())
                && nonNull(result.getStatusDetails().getMessage())
                && result.getStatusDetails().getMessage().matches(category.getMessageRegex());
        boolean matchesTrace = isNull(category.getTraceRegex())
                || nonNull(result.getStatusDetails())
                && nonNull(result.getStatusDetails().getTrace())
                && result.getStatusDetails().getTrace().matches(category.getTraceRegex());
        return matchesStatus && matchesMessage && matchesTrace;
    }
}
