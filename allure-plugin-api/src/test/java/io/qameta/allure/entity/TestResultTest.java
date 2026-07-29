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
package io.qameta.allure.entity;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestResultTest {

    /**
     * Verifies retry identity is derived by dot-joining the test case and parameter hashes.
     */
    @Description
    @Test
    void shouldDeriveRetryHash() {
        final TestResult result = new TestResult()
                .setTestCaseHash("f103a4030d01bbae3f83d8a344f63a47")
                .setParametersHash("3c7cc1db0a01f29eabb2ac10e3cf6788");

        assertThat(result.getRetryHash())
                .isEqualTo("f103a4030d01bbae3f83d8a344f63a47.3c7cc1db0a01f29eabb2ac10e3cf6788");
    }

    /**
     * Verifies both component hashes are required to derive a retry hash.
     */
    @Description
    @Test
    void shouldRequireBothHashesForRetryHash() {
        final TestResult withoutTestCaseHash = new TestResult()
                .setParametersHash("parameters");
        final TestResult withoutParametersHash = new TestResult()
                .setTestCaseHash("test-case");

        assertThat(withoutTestCaseHash.getRetryHash()).isNull();
        assertThat(withoutParametersHash.getRetryHash()).isNull();
    }
}
