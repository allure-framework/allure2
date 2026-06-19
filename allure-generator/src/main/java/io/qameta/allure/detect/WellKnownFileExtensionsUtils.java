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
package io.qameta.allure.detect;

import org.apache.commons.io.FilenameUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The extension map is ported from <a href="https://tika.apache.org/">tika-core:2.9.1</a>.
 *
 * @author charlie (Dmitry Baev).
 */
public final class WellKnownFileExtensionsUtils {

    private static final String ALLURE_HTTP_EXCHANGE = "httpexchange";
    private static final String OCTET_STREAM = "application/octet-stream";

    public static final Map<String, String> EXT_TO_MIME_TYPE = WellKnownMimeTypes.EXT_TO_MIME_TYPE;
    public static final Map<String, String> MIME_TYPE_TO_EXT;

    static {
        final Map<String, String> extToMimeType = new HashMap<>();
        for (Map.Entry<String, String> stringStringEntry : WellKnownMimeTypes.EXT_TO_MIME_TYPE.entrySet()) {
            extToMimeType.putIfAbsent(stringStringEntry.getValue(), stringStringEntry.getKey());
        }
        extToMimeType.put("application/vnd.allure.http", ALLURE_HTTP_EXCHANGE);
        MIME_TYPE_TO_EXT = Collections.unmodifiableMap(extToMimeType);
    }

    private WellKnownFileExtensionsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String lookup(final String fileName) {
        final String extension = FilenameUtils.getExtension(fileName);
        if (Objects.isNull(extension) || extension.isEmpty()) {
            return null;
        }

        return EXT_TO_MIME_TYPE.get(extension);
    }

    public static String getExtensionByMimeType(final String contentType) {
        if (OCTET_STREAM.equals(contentType)) {
            return "";
        }
        return MIME_TYPE_TO_EXT.getOrDefault(contentType, "");
    }
}
