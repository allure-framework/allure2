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
package io.qameta.allure.allure2;

import io.qameta.allure.Description;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.TestResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static io.qameta.allure.allure2.Allure2Plugin.getParametersHash;
import static io.qameta.allure.allure2.Allure2Plugin.getTestCaseHash;
import static io.qameta.allure.model.Parameter.Mode.HIDDEN;
import static io.qameta.allure.model.Parameter.Mode.MASKED;
import static org.assertj.core.api.Assertions.assertThat;

class Allure2PluginIdentityTest {

    /**
     * Verifies identity hash compatibility, including raw hidden and masked parameter values.
     */
    @Description
    @Test
    void shouldMatchAllure3IdentifierContract() {
        final TestResult result = new TestResult()
                .setTestCaseId("org.example.ExampleTest.parameterized")
                .setHistoryId("adapter-provided-history-id")
                .setParameters(
                        Arrays.asList(
                                parameter("z", "value-z"),
                                parameter("ignored", "ignored-value").setExcluded(true),
                                parameter("m", "secret").setMode(HIDDEN),
                                parameter("a", "value-a"),
                                parameter("b", "masked-secret").setMode(MASKED)
                        )
                );

        final io.qameta.allure.entity.TestResult identity = calculateIdentity(result);

        assertThat(identity.getTestCaseHash()).isEqualTo("c6afc390a37d516b75b0889d60eadf7b");
        assertThat(identity.getParametersHash()).isEqualTo("a0995c760832a18e23b1ad9d26b6ede1");
        assertThat(identity.getRetryHash())
                .isEqualTo("c6afc390a37d516b75b0889d60eadf7b.a0995c760832a18e23b1ad9d26b6ede1");
    }

    /**
     * Verifies adapter-provided history ids do not participate in identity hash calculation.
     */
    @Description
    @Test
    void shouldIgnoreProvidedHistoryId() {
        final TestResult result = new TestResult()
                .setTestCaseId("test-case-id")
                .setHistoryId("first-history-id")
                .setParameters(Collections.singletonList(parameter("argument", "value")));
        final io.qameta.allure.entity.TestResult first = calculateIdentity(result);

        result.setHistoryId("second-history-id");

        final io.qameta.allure.entity.TestResult second = calculateIdentity(result);

        assertThat(second.getTestCaseHash()).isEqualTo(first.getTestCaseHash());
        assertThat(second.getParametersHash()).isEqualTo(first.getParametersHash());
        assertThat(second.getRetryHash()).isEqualTo(first.getRetryHash());
        assertThat(second.getTestCaseHash()).isEqualTo("97a2c529ed683cc603ce988040c657f8");
        assertThat(second.getParametersHash()).isEqualTo("310bf7d9fc9765b03f3a78f1816f40a8");
        assertThat(second.getRetryHash())
                .isEqualTo("97a2c529ed683cc603ce988040c657f8.310bf7d9fc9765b03f3a78f1816f40a8");
    }

    /**
     * Verifies parameter variants receive separate retry hashes even when an adapter reuses its history id.
     */
    @Description
    @Test
    void shouldSeparateRetriesByParameters() {
        final TestResult first = new TestResult()
                .setTestCaseId("test-case-id")
                .setHistoryId("shared-adapter-history-id")
                .setParameters(Collections.singletonList(parameter("argument", "first")));
        final TestResult second = new TestResult()
                .setTestCaseId("test-case-id")
                .setHistoryId("shared-adapter-history-id")
                .setParameters(Collections.singletonList(parameter("argument", "second")));

        assertThat(calculateIdentity(first).getRetryHash()).isNotEqualTo(calculateIdentity(second).getRetryHash());
    }

    /**
     * Verifies exact duplicate parameter pairs do not change parameter identity.
     */
    @Description
    @Test
    void shouldCollapseExactDuplicateParameters() {
        final TestResult withDuplicate = new TestResult()
                .setParameters(
                        Arrays.asList(
                                parameter("argument", "value"),
                                parameter("argument", "value")
                        )
                );
        final TestResult withoutDuplicate = new TestResult()
                .setParameters(Collections.singletonList(parameter("argument", "value")));

        assertThat(getParametersHash(withDuplicate))
                .isEqualTo("310bf7d9fc9765b03f3a78f1816f40a8")
                .isEqualTo(getParametersHash(withoutDuplicate));
    }

    /**
     * Verifies parameters with the same name and different values remain part of parameter identity.
     */
    @Description
    @Test
    void shouldKeepSameNamedParametersWithDifferentValues() {
        final TestResult result = new TestResult()
                .setParameters(
                        Arrays.asList(
                                parameter("argument", "second"),
                                parameter("argument", "first")
                        )
                );

        assertThat(getParametersHash(result)).isEqualTo("6e29f32eaf2b41fc71988ccc7ad13ac2");
    }

    /**
     * Verifies parameter ordering compares UTF-8 bytes instead of UTF-16 code units.
     */
    @Description
    @Test
    void shouldSortParametersByUtf8Bytes() {
        final TestResult result = new TestResult()
                .setParameters(
                        Arrays.asList(
                                parameter("\uD83D\uDE00", "value"),
                                parameter("\uE000", "value")
                        )
                );

        assertThat(getParametersHash(result)).isEqualTo("a6cd7ed419df4da92294adabd4ff4904");
    }

    /**
     * Verifies invalid and excluded parameters are discarded and null values use the compatibility sentinel.
     */
    @Description
    @Test
    void shouldNormalizeParametersBeforeHashing() {
        final TestResult result = new TestResult()
                .setParameters(
                        Arrays.asList(
                                null,
                                parameter(null, "ignored"),
                                parameter("", "ignored"),
                                parameter("excluded", "ignored").setExcluded(true),
                                parameter("missing", null)
                        )
                );

        assertThat(getParametersHash(result)).isEqualTo("3bf9dbcebd98256fba82c63e37384e7d");
    }

    /**
     * Verifies full name fallback compatibility with Allure 3 for results without a test case id.
     */
    @Description
    @Test
    void shouldUseFullNameWhenTestCaseIdIsMissing() {
        final TestResult result = new TestResult()
                .setFullName("org.example.ExampleTest.parameterized(java.lang.String)");

        final io.qameta.allure.entity.TestResult identity = calculateIdentity(result);

        assertThat(identity.getTestCaseHash()).isEqualTo("72aa5b1c6dc992de123d1924d01f74d0");
        assertThat(identity.getParametersHash()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        assertThat(identity.getRetryHash())
                .isEqualTo("72aa5b1c6dc992de123d1924d01f74d0.d41d8cd98f00b204e9800998ecf8427e");
    }

    /**
     * Verifies external Allure ids do not change execution identity hashes.
     */
    @Description
    @Test
    void shouldIgnoreAllureIdLabelsForIdentityHashes() {
        final TestResult result = new TestResult()
                .setTestCaseId("test-case-id")
                .setParameters(Collections.singletonList(parameter("argument", "value")));
        final io.qameta.allure.entity.TestResult withoutAllureId = calculateIdentity(result);

        result.setLabels(Collections.singletonList(new Label().setName("AS_ID").setValue("123")));
        final io.qameta.allure.entity.TestResult withAsId = calculateIdentity(result);

        result.setLabels(Collections.singletonList(new Label().setName("ALLURE_ID").setValue("123")));
        final io.qameta.allure.entity.TestResult withAllureId = calculateIdentity(result);

        assertThat(withAsId.getTestCaseHash()).isEqualTo(withoutAllureId.getTestCaseHash());
        assertThat(withAsId.getParametersHash()).isEqualTo(withoutAllureId.getParametersHash());
        assertThat(withAsId.getRetryHash()).isEqualTo(withoutAllureId.getRetryHash());
        assertThat(withAllureId.getTestCaseHash()).isEqualTo(withoutAllureId.getTestCaseHash());
        assertThat(withAllureId.getParametersHash()).isEqualTo(withoutAllureId.getParametersHash());
        assertThat(withAllureId.getRetryHash()).isEqualTo(withoutAllureId.getRetryHash());

        result.setTestCaseId(null);
        final io.qameta.allure.entity.TestResult withoutTestCaseIdentity = calculateIdentity(result);

        assertThat(withoutTestCaseIdentity.getTestCaseHash()).isNull();
        assertThat(withoutTestCaseIdentity.getRetryHash()).isNull();
    }

    /**
     * Verifies fallback test case ids are ignored when no cross-report test-case catalog exists.
     */
    @Description
    @Test
    void shouldIgnoreFallbackTestCaseIdWithoutCatalog() {
        final TestResult result = new TestResult()
                .setTestCaseId("current-test-case-id");
        final io.qameta.allure.entity.TestResult withoutFallback = calculateIdentity(result);

        result.setLabels(
                Collections.singletonList(
                        new Label().setName("_fallbackTestCaseId").setValue("previous-test-case-id")
                )
        );
        final io.qameta.allure.entity.TestResult withFallback = calculateIdentity(result);

        assertThat(withFallback.getTestCaseHash()).isEqualTo(withoutFallback.getTestCaseHash());
        assertThat(withFallback.getRetryHash()).isEqualTo(withoutFallback.getRetryHash());

        result.setTestCaseId(null);
        final io.qameta.allure.entity.TestResult withoutTestCaseIdentity = calculateIdentity(result);

        assertThat(withoutTestCaseIdentity.getTestCaseHash()).isNull();
        assertThat(withoutTestCaseIdentity.getRetryHash()).isNull();
    }

    /**
     * Verifies results without a stable test identity retain only their parameter hash.
     */
    @Description
    @Test
    void shouldNotCalculateRetryHashWithoutTestIdentity() {
        final io.qameta.allure.entity.TestResult identity = calculateIdentity(new TestResult());

        assertThat(identity.getTestCaseHash()).isNull();
        assertThat(identity.getParametersHash()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        assertThat(identity.getRetryHash()).isNull();
    }

    private static io.qameta.allure.entity.TestResult calculateIdentity(final TestResult result) {
        return new io.qameta.allure.entity.TestResult()
                .setTestCaseHash(getTestCaseHash(result))
                .setParametersHash(getParametersHash(result));
    }

    private static Parameter parameter(final String name, final String value) {
        return new Parameter().setName(name).setValue(value);
    }
}
