/*
 *  Copyright 2016-2026 Qameta Software Inc
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

import java.nio.file.Path;

/**
 * Storage for generated report content and additional integration artifacts.
 * Report content is embedded in single-file mode. Additional artifacts are
 * written beside the HTML by the single-file report storage.
 * The method selects the destination; output paths do not affect embedding.
 *
 * @author charlie (Dmitry Baev).
 */
public interface ReportStorage {

    void addDataJson(String name, Object data);

    void addDataBinary(String name, byte[] data);

    void addDataFile(String name, Path file);

    /**
     * Store additional JSON data, such as a CI summary, outside the embedded report.
     * The default implementation preserves compatibility with existing storage implementations.
     *
     * @param name the relative output path.
     * @param data the data to serialize.
     */
    default void addAdditionalJson(final String name, final Object data) {
        addDataJson(name, data);
    }

    /**
     * Store additional binary data outside the embedded report.
     *
     * @param name the relative output path.
     * @param data the data to write.
     */
    default void addAdditionalBinary(final String name, final byte[] data) {
        addDataBinary(name, data);
    }

    /**
     * Copy an additional file outside the embedded report.
     *
     * @param name the relative output path.
     * @param file the source file.
     */
    default void addAdditionalFile(final String name, final Path file) {
        addDataFile(name, file);
    }

}
