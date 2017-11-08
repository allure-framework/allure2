package io.qameta.allure.category;

import io.qameta.allure.entity.TestResult;
import io.qameta.allure.trend.TrendItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class CategoriesTrendItem extends TrendItem {

    public void increaseCategories(final TestResult result) {
        result.<List<Category>>getExtraBlock("categories", new ArrayList<>()).stream()
                .map(Category::getName)
                .forEach(this::increaseCategories);
    }

    private void increaseCategories(final String name) {
        increaseMetric(name);
    }

}
