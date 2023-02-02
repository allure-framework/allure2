/*
 *  Copyright 2016-2023 Qameta Software OÜ
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

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class RandomUidContextTest {

    @Test
    void shouldCreateRandomUidContext() {
        final RandomUidContext context = new RandomUidContext();
        assertThat(context.getValue())
                .isNotNull();
    }

    @Test
    void shouldGenerateRandomValues() {
        final RandomUidContext context = new RandomUidContext();
        final Supplier<String> generator = context.getValue();

        final String first = generator.get();
        final String second = generator.get();
        assertThat(first)
                .isNotBlank()
                .isNotEqualTo(second);

        assertThat(second)
                .isNotBlank();
    }
}
