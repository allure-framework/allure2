package io.qameta.allure.context;

import freemarker.template.Configuration;
import io.qameta.allure.Context;

/**
 * Context that stores freemarker configuration.
 *
 * @since 2.0
 */
public class FreemarkerContext implements Context<Configuration> {

    private static final String BASE_PACKAGE_PATH = "tpl";

    private final Configuration configuration;

    public FreemarkerContext() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_23);
        this.configuration.setLocalizedLookup(false);
        this.configuration.setTemplateUpdateDelayMilliseconds(0);
        this.configuration.setClassLoaderForTemplateLoading(getClass().getClassLoader(), BASE_PACKAGE_PATH);
    }

    @Override
    public Configuration getValue() {
        return configuration;
    }
}
