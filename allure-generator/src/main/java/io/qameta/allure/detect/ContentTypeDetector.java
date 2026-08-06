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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Detects content types from well-known file extensions and file headers.
 */
public final class ContentTypeDetector {

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    // So far the maximum offset is 512 for supported files.
    private static final int MAGIC_HEADER_LENGTH = 1024;

    private ContentTypeDetector() {
        throw new IllegalStateException("Do not instantiate");
    }

    public static String probeContentType(final InputStream stream,
                                          final String fileName)
            throws IOException {
        final String contentTypeByName = WellKnownFileExtensionsUtils.lookup(fileName);
        if (Objects.nonNull(contentTypeByName)) {
            return contentTypeByName;
        }

        final byte[] buffer = new byte[MAGIC_HEADER_LENGTH];
        final int bytesRead = IOUtils.read(stream, buffer);
        if (bytesRead <= 0) {
            return APPLICATION_OCTET_STREAM;
        }
        return Optional.ofNullable(
                MagicBytesContentTypeDetector.detectContentType(Arrays.copyOf(buffer, bytesRead))
        ).orElse(APPLICATION_OCTET_STREAM);
    }
}
