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
package io.qameta.allure.context;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class RandomUidContextTest {

    /**
     * Verifies that the random UID context creates a generator.
     * The test checks that consumers receive a non-null supplier.
     */
    @Description
    @Test
    void shouldCreateRandomUidContext() {
        final RandomUidContext context = new RandomUidContext();
        assertThat(context.getValue())
                .isNotNull();
    }

    /**
     * Verifies that the random UID supplier returns usable unique values.
     * The test checks two generated values are non-blank and different.
     */
    @Description
    @Test
    void shouldGenerateRandomValues() {
        final RandomUidContext context = new RandomUidContext();
        final Supplier<String> generator = context.getValue();

        final String first = generator.get();
        final String second = generator.get();

        Allure.step("Record generated UID values", () -> Allure.addAttachment(
                "generated-uids.txt",
                "text/plain",
                String.format(
                        "first=%s%nsecond=%s%nfirstBlank=%s%nsecondBlank=%s%nsame=%s%n",
                        first,
                        second,
                        first.trim().isEmpty(),
                        second.trim().isEmpty(),
                        first.equals(second)
                ),
                ".txt"
        ));

        assertThat(first)
                .isNotBlank()
                .isNotEqualTo(second);

        assertThat(second)
                .isNotBlank();
    }
}
