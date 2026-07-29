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

import static io.qameta.allure.allure2.Allure2RetryHashCalculator.calculate;
import static io.qameta.allure.model.Parameter.Mode.HIDDEN;
import static io.qameta.allure.model.Parameter.Mode.MASKED;
import static org.assertj.core.api.Assertions.assertThat;

class Allure2RetryHashCalculatorTest {

    /**
     * Verifies history and retry identifier compatibility with Allure 3, including raw hidden and masked values.
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

        final Allure2RetryHashCalculator.Identifiers identifiers = calculate(result);

        assertThat(identifiers.getHistoryId())
                .isEqualTo("c6afc390a37d516b75b0889d60eadf7b.a0995c760832a18e23b1ad9d26b6ede1");
        assertThat(identifiers.getRetryHash()).isEqualTo("fbd6d583218739ca9c0497362505bf43");
    }

    /**
     * Verifies adapter-provided history ids do not participate in generated identifier calculation.
     */
    @Description
    @Test
    void shouldIgnoreProvidedHistoryId() {
        final TestResult result = new TestResult()
                .setTestCaseId("test-case-id")
                .setHistoryId("first-history-id")
                .setParameters(Collections.singletonList(parameter("argument", "value")));
        final Allure2RetryHashCalculator.Identifiers first = calculate(result);

        result.setHistoryId("second-history-id");

        final Allure2RetryHashCalculator.Identifiers second = calculate(result);
        assertThat(second.getHistoryId()).isEqualTo(first.getHistoryId());
        assertThat(second.getRetryHash()).isEqualTo(first.getRetryHash());
    }

    /**
     * Verifies parameter variants receive separate history ids even when an adapter reuses its history id.
     */
    @Description
    @Test
    void shouldSeparateHistoryByParameters() {
        final TestResult first = new TestResult()
                .setTestCaseId("test-case-id")
                .setHistoryId("shared-adapter-history-id")
                .setParameters(Collections.singletonList(parameter("argument", "first")));
        final TestResult second = new TestResult()
                .setTestCaseId("test-case-id")
                .setHistoryId("shared-adapter-history-id")
                .setParameters(Collections.singletonList(parameter("argument", "second")));

        assertThat(calculate(first).getHistoryId()).isNotEqualTo(calculate(second).getHistoryId());
    }

    /**
     * Verifies full name fallback compatibility with Allure 3 for results without a test case id.
     */
    @Description
    @Test
    void shouldUseFullNameWhenTestCaseIdIsMissing() {
        final TestResult result = new TestResult()
                .setFullName("org.example.ExampleTest.parameterized(java.lang.String)");

        assertThat(calculate(result).getRetryHash()).isEqualTo("50edf5c4826881a53ed73dc3350766a7");
    }

    /**
     * Verifies explicit Allure ids take precedence over adapter test case ids.
     */
    @Description
    @Test
    void shouldPreferAllureId() {
        final TestResult result = new TestResult()
                .setTestCaseId("test-case-id")
                .setLabels(Collections.singletonList(new Label().setName("AS_ID").setValue("123")));

        assertThat(calculate(result).getRetryHash()).isEqualTo("4619a6b0bbfb4dafaba4e1044eee6eac");
    }

    /**
     * Verifies results without a stable test identity are not grouped as retries.
     */
    @Description
    @Test
    void shouldNotCalculateRetryHashWithoutTestIdentity() {
        final Allure2RetryHashCalculator.Identifiers identifiers = calculate(new TestResult());

        assertThat(identifiers.getHistoryId()).isNull();
        assertThat(identifiers.getRetryHash()).isNull();
    }

    private static Parameter parameter(final String name, final String value) {
        return new Parameter().setName(name).setValue(value);
    }
}
