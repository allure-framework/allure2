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

import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * The name of environment variable that disables analytics.
     */
    public static final String NO_ANALYTICS = "ALLURE_NO_ANALYTICS";

    private Constants() {
        throw new IllegalStateException("Do not instance");
    }

    /**
     * Build path within plugins directory.
     *
     * @param pathItems the path items.
     * @return the path within plugins directory.
     */
    public static String pluginPath(final String... pathItems) {
        return path(PLUGINS_DIR, pathItems);
    }

    /**
     * Build path within export directory.
     *
     * @param pathItems the path items.
     * @return the path within export directory.
     */
    public static String exportPath(final String... pathItems) {
        return path(EXPORT_DIR, pathItems);
    }

    /**
     * Build path within data directory.
     *
     * @param pathItems the path items.
     * @return the path within data directory.
     */
    public static String dataPath(final String... pathItems) {
        return path(DATA_DIR, pathItems);
    }

    /**
     * Build path within widgets directory.
     *
     * @param pathItems the path items.
     * @return the path within widgets directory.
     */
    public static String widgetsPath(final String... pathItems) {
        return path(WIDGETS_DIR, pathItems);
    }

    /**
     * Build path within history directory.
     *
     * @param pathItems the path items.
     * @return the path within history directory.
     */
    public static String historyPath(final String... pathItems) {
        return path(HISTORY_DIR, pathItems);
    }

    /**
     * Build path from specified path items.
     *
     * @param first  the first path item.
     * @param others other path items.
     * @return the path.
     */
    public static String path(final String first, final String... others) {
        return Stream
                .concat(
                        Stream.of(first),
                        Stream.of(others)
                )
                .collect(Collectors.joining("/"));
    }
}
