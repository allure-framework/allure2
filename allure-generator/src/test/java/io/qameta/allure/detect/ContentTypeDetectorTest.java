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

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for combined extension and magic-byte content-type detection.
 */
class ContentTypeDetectorTest {

    /**
     * Verifies a well-known extension is resolved without reading file content.
     * The test checks an unavailable stream does not affect extension detection.
     */
    @Description
    @Test
    void shouldPreferContentTypeFromFileName() throws Exception {
        final InputStream unreadable = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("must not read");
            }
        };

        assertThat(ContentTypeDetector.probeContentType(unreadable, "preview.html"))
                .isEqualTo("text/html");
    }

    /**
     * Verifies unknown extensions fall back to magic-byte detection.
     * The test checks a PNG header is recognized from file content.
     */
    @Description
    @Test
    void shouldDetectContentTypeFromMagicBytes() throws Exception {
        final byte[] pngHeader = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };

        assertThat(
                ContentTypeDetector.probeContentType(
                        new ByteArrayInputStream(pngHeader),
                        "attachment"
                )
        ).isEqualTo("image/png");
    }

    /**
     * Verifies magic-byte probing fills the header across short stream reads.
     * The test checks a tar signature beyond the first chunk remains detectable.
     */
    @Description
    @Test
    void shouldDetectContentTypeAcrossShortReads() throws Exception {
        final byte[] tarContent = new byte[512];
        final byte[] tarSignature = new byte[]{'u', 's', 't', 'a', 'r', ' ', ' ', 0};
        System.arraycopy(tarSignature, 0, tarContent, 257, tarSignature.length);
        final InputStream shortReadStream = new ByteArrayInputStream(tarContent) {
            @Override
            public synchronized int read(final byte[] buffer,
                                         final int offset,
                                         final int length) {
                return super.read(buffer, offset, Math.min(length, 64));
            }
        };

        assertThat(ContentTypeDetector.probeContentType(shortReadStream, "attachment"))
                .isEqualTo("application/x-gtar");
    }

    /**
     * Verifies empty unknown content uses the generic binary type.
     * The test checks callers receive a non-null safe default.
     */
    @Description
    @Test
    void shouldUseOctetStreamForEmptyUnknownContent() throws Exception {
        assertThat(
                ContentTypeDetector.probeContentType(
                        new ByteArrayInputStream(new byte[0]),
                        "attachment"
                )
        ).isEqualTo(ContentTypeDetector.APPLICATION_OCTET_STREAM);
    }
}
