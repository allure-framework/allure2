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
package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.util.List;

/**
 * Widget extension.
 *
 * @since 2.0,
 * @deprecated use {@link Aggregator} instead.
 */
@Deprecated
public interface Widget extends Extension {

    /**
     * Returns widget data. The data will be marshaled to JSON and
     * available in frontend widget plugin. To disable the widget return null.
     *
     * @param configuration the report configuration.
     * @param launches      the parsed tests results.
     * @return widget data. Null if widget is disabled.
     */
    Object getData(Configuration configuration, List<LaunchResults> launches);

    /**
     * Returns unique widget name. Should not be a null.
     *
     * @return widget name.
     */
    String getName();

}
