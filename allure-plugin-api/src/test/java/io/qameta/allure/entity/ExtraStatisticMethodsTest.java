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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class ExtraStatisticMethodsTest {

    @Test
    void shouldGetStatusForStatistic() {
        final Statistic first = new Statistic().setFailed(2L).setPassed(1L);
        final Statistic second = new Statistic().setPassed(4L).setBroken(1L);
        final Statistic third = new Statistic().setPassed(1L).setSkipped(4L);
        assertThat(Arrays.asList(first, second, third))
                .extracting(Statistic::getStatus)
                .containsExactly(
                        Status.FAILED, Status.BROKEN, Status.PASSED
                );
    }

    @Test
    void shouldGetByStatus() {
        final Statistic statistic = new Statistic().setFailed(2L).setPassed(1L).setBroken(4L);
        assertThat(Arrays.asList(Status.FAILED, Status.BROKEN, Status.PASSED, Status.UNKNOWN))
                .extracting(statistic::get)
                .containsExactly(2L, 4L, 1L, 0L);
    }
}
