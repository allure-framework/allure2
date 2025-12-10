/*
 *  Copyright 2016-2024 Qameta Software Inc
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class WellKnownFileExtensionsUtilsTest {

    static Stream<Arguments> expectedContentTypes() {
        return Stream.of(
                Arguments.of("sample.png", "image/png"),
                Arguments.of("some-path/to/sample.gif", "image/gif"),
                Arguments.of("C://System Files x86/custom folder/sample.png", "image/png"),
                Arguments.of("/sample.tiff", "image/tiff"),
                Arguments.of("http://examplle.org/sample.jpeg", "image/jpeg"),
                Arguments.of("tar.gz.svg", "image/svg+xml"),
                Arguments.of("archive.tar.gz", "application/gzip"),
                Arguments.of("", null),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("expectedContentTypes")
    void shouldDetectContentType(final String resourceName, final String expectedContentType) {
        final String detectedContentType = WellKnownFileExtensionsUtils.lookup(resourceName);

        assertThat(detectedContentType)
                .isEqualTo(expectedContentType);
    }

    static Stream<Arguments> expectedExtensions() {
        return Stream.of(
                Arguments.of("image/png", "png"),
                Arguments.of("image/jpeg", "jpg"),
                Arguments.of("image/gif", "gif"),
                Arguments.of("image/tiff", "tiff"),
                Arguments.of("image/svg+xml", "svg"),
                Arguments.of("application/gzip", "tgz"),
                Arguments.of("text/plain", "txt"),
                Arguments.of("application/octet-stream", ""),
                Arguments.of("", ""),
                Arguments.of(null, "")
        );
    }

    @ParameterizedTest
    @MethodSource("expectedExtensions")
    void shouldReturnExtensionByContentType(final String contentType, final String expectedExtension) {
        final String detectedContentType = WellKnownFileExtensionsUtils.getExtensionByMimeType(contentType);

        assertThat(detectedContentType)
                .isEqualTo(expectedExtension);
    }

}
