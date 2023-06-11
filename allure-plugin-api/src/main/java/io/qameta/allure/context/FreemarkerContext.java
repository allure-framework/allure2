/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

    /**
     * Creates a default context that stores Freemarker configuration.
     */
    public FreemarkerContext() {
        this(BASE_PACKAGE_PATH);
    }

    /**
     * Creates a new context that stores Freemarker configuration.
     *
     * @param basePackagePath The package that contains the templates, in path ({@code /}-separated) format. Note that
     *                        path components should be separated by forward slashes independently of the separator
     *                        character used by the underlying operating system. This parameter can't be {@code null}.
     */
    public FreemarkerContext(final String basePackagePath) {
        this.configuration = new Configuration(Configuration.VERSION_2_3_23);
        this.configuration.setLocalizedLookup(false);
        this.configuration.setTemplateUpdateDelayMilliseconds(0);
        this.configuration.setClassLoaderForTemplateLoading(getClass().getClassLoader(), basePackagePath);
    }

    @Override
    public Configuration getValue() {
        return configuration;
    }
}
