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
