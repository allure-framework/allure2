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
package io.qameta.allure.entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class LabelsTest {

    @Test
    void shouldFindLabelsInEmptyArray() {
        final Optional<String> found = new TestResult().findOneLabel("hey");
        assertThat(found)
                .isEmpty();
    }

    @Test
    void shouldFindOneWithNullValue() {
        final TestResult result = new TestResult();
        result.getLabels().add(new Label().setName("hey").setValue(null));
        final Optional<String> found = result.findOneLabel("hey");
        assertThat(found)
                .isEmpty();
    }

    @Test
    void shouldFindAllWithNullValue() {
        final TestResult result = new TestResult();
        result.getLabels().add(new Label().setName("hey").setValue(null));
        result.getLabels().add(new Label().setName("hey").setValue("a"));
        result.getLabels().add(new Label().setName("hey").setValue("b"));
        final List<String> found = result.findAllLabels("hey");
        assertThat(found)
                .containsExactlyInAnyOrder(null, "a", "b");
    }
}
