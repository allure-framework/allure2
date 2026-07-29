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
     * Verifies explicit Allure ids take precedence over adapter test case ids.
     */
    @Description
    @Test
    void shouldPreferAllureId() {
        final TestResult result = new TestResult()
                .setTestCaseId("test-case-id")
                .setLabels(Collections.singletonList(new Label().setName("AS_ID").setValue("123")));

        final io.qameta.allure.entity.TestResult identity = calculateIdentity(result);

        assertThat(identity.getTestCaseHash()).isEqualTo("094bc5de67cbc4ea04b49808c98bbf69");
        assertThat(identity.getRetryHash())
                .isEqualTo("094bc5de67cbc4ea04b49808c98bbf69.d41d8cd98f00b204e9800998ecf8427e");
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
