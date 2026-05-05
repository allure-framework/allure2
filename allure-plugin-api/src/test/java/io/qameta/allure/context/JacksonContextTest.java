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

import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonContextTest {

    /**
     * Verifies that the Jackson context creates an object mapper.
     * The test checks that consumers receive a non-null mapper instance.
     */
    @Description
    @Test
    void shouldCreateJacksonContext() {
        final JacksonContext context = new JacksonContext();
        assertThat(context.getValue())
                .isNotNull();
    }

    /**
     * Verifies that the Jackson context uses compact JSON output by default.
     * The test checks that indentation is disabled in the mapper serialization config.
     */
    @Description
    @Test
    void shouldUseMinified() {
        final JacksonContext context = new JacksonContext();
        final SerializationConfig config = context.getValue().getSerializationConfig();
        assertThat(config.isEnabled(SerializationFeature.INDENT_OUTPUT))
                .isFalse();
    }
}
