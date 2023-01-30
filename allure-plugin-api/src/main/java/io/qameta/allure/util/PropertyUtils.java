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
package io.qameta.allure.util;

import io.qameta.allure.exception.PropertyNotFoundException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Properties;

/**
 * Utility methods for using properties.
 */
public final class PropertyUtils {

    private PropertyUtils() {
    }

    public static Optional<String> getProperty(final String key) {
        final Properties properties = new Properties();
        properties.putAll(System.getenv());
        return Optional.ofNullable(properties.getProperty(key)).filter(StringUtils::isNotBlank);
    }

    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    public static String requireProperty(final String key) {
        return getProperty(key).orElseThrow(() -> new PropertyNotFoundException(key));
    }

}
