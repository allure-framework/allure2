package io.qameta.allure.categories;

import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeResultAggregator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.qameta.allure.categories.CategoriesPlugin.CATEGORIES;

/**
 * @author charlie (Dmitry Baev).
 */
public class CategoriesResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(final TestCaseResult result) {
        final List<Category> categories = result.getExtraBlock(CATEGORIES);
        final String message = result.getStatusMessage().orElse("Empty message");
        return Arrays.asList(
                TreeGroup.values(categories.stream().map(Category::getName).toArray(String[]::new)),
                TreeGroup.values(message)
        );
    }

    @Override
    protected boolean shouldAggregate(final TestCaseResult result) {
        final List<Category> categories = result.getExtraBlock(CATEGORIES);
        return Objects.nonNull(categories) && !categories.isEmpty();
    }
}
