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

/**
 * @author charlie (Dmitry Baev).
 * @since 2.7
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public final class Constants {

    /**
     * The name of directory that contains widgets data.
     */
    public static final String WIDGETS_DIR = "widgets";

    /**
     * The name of directory with main report data.
     */
    public static final String DATA_DIR = "data";

    /**
     * The name of directory with report plugins.
     */
    public static final String PLUGINS_DIR = "plugins";

    /**
     * The name of directory with exported data.
     */
    public static final String EXPORT_DIR = "export";

    /**
     * The name of directory with historical data.
     */
    public static final String HISTORY_DIR = "history";

    private Constants() {
        throw new IllegalStateException("Do not instance");
    }
}
