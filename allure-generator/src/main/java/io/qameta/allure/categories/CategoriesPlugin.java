package io.qameta.allure.categories;

import com.google.inject.multibindings.Multibinder;
import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.TestRunDetailsReader;

/**
 * @author charlie (Dmitry Baev).
 */
public class CategoriesPlugin extends AbstractPlugin {

    public static final String CATEGORIES_JSON = "categories.json";

    public static final String CATEGORIES = "categories";

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestRunDetailsReader.class)
                .addBinding().to(CategoriesReader.class);

        processor(CategoriesProcessor.class);
        aggregateResults(CategoriesResultAggregator.class)
                .toReportData(CATEGORIES_JSON);
    }
}
