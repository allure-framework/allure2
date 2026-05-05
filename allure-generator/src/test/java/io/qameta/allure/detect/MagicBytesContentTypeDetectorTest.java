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

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;

import static io.qameta.allure.testdata.TestData.toHex;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class MagicBytesContentTypeDetectorTest {

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("sample.png", "image/png"),
                Arguments.of("sample.gif", "image/gif"),
                Arguments.of("sample.png", "image/png"),
                Arguments.of("sample.tiff", "image/tiff"),
                Arguments.of("sample.jpeg", "image/jpeg"),
                Arguments.of("sample.svg", "image/svg+xml"),
                Arguments.of("short.svg", "image/svg+xml")
        );
    }

    /**
     * Verifies detecting content type for magic byte content detection.
     */
    @Description
    @ParameterizedTest
    @MethodSource("data")
    void shouldDetectContentType(final String resourceName, final String expectedContentType) throws IOException {
        Allure.parameter("resourceName", resourceName);
        Allure.parameter("expectedContentType", expectedContentType);
        final String resource = "sample-files-to-detect/" + resourceName;
        byte[] bytes = Allure.step("Read sample file " + resource, () -> {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream(resource)) {
                IOUtils.copy(Objects.requireNonNull(stream, "no resource found:" + resource), bos);
            }
            final byte[] content = bos.toByteArray();
            Allure.addAttachment(resourceName, "text/plain", describeMagicBytes(content));
            return content;
        });
        final String detectedContentType = Allure.step(
                "Detect content type from magic bytes",
                () -> MagicBytesContentTypeDetector.detectContentType(bytes)
        );

        assertThat(detectedContentType)
                .isEqualTo(expectedContentType);
    }

    private String describeMagicBytes(final byte[] content) {
        return String.format(
                "length=%d%nhexPreview=%s%n",
                content.length,
                toHex(content, 32)
        );
    }
}
