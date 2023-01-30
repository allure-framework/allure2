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
package io.qameta.allure.datetime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author charlie (Dmitry Baev).
 */
class ZonedDateTimeParserTest {

    static Stream<Arguments> data() {
        return Stream.of(
                arguments("2018-05-31T14:05:25.155Z", 1527775525155L),
                arguments("2018-05-06T07:41:51Z", 1525592511000L),
                arguments("2018-05-31T14:05:25.155+03:00", 1527775525155L - TimeUnit.HOURS.toMillis(3)),
                arguments("2018-05-31T14:05:25.155-07:00", 1527775525155L + TimeUnit.HOURS.toMillis(7))
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldParseZonedDateTime(final String time, final Long expected) {
        final Optional<Long> parsed = new ZonedDateTimeParser().getEpochMilli(time);

        assertThat(parsed)
                .hasValue(expected);
    }

}
