package io.qameta.allure.category;

import io.qameta.allure.entity.TestResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class CategoriesTrendItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long buildOrder;
    protected String reportUrl;
    protected String reportName;
    protected Map<String, Integer> categories = new HashMap<>();

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ void updateCategories(final TestResult result) {
        result
            .<List<Category>>getExtraBlock("categories", new ArrayList<>())
            .stream()
            .map(Category::getName)
            .forEach(this::updateCategories);
    }

    private void updateCategories(final String categoryName) {
        if (Objects.isNull(this.categories.get(categoryName))) {
            this.categories.put(categoryName, 1);
        } else {
            this.categories.put(categoryName, this.categories.get(categoryName) + 1);
        }
    }
}
