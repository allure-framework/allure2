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

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class CompositeDateTimeParserTest {

    @Test
    void shouldReturnFirstParsed() {
        final CompositeDateTimeParser parser = new CompositeDateTimeParser(
                new ZonedDateTimeParser(),
                new LocalDateTimeParser(ZoneOffset.UTC)
        );

        final Optional<Long> parsed1 = parser.getEpochMilli("2017-10-05T10:36:22");

        assertThat(parsed1)
                .hasValue(1507199782000L);

        final Optional<Long> parsed2 = parser.getEpochMilli("2018-05-31T14:05:25.155Z");

        assertThat(parsed2)
                .hasValue(1527775525155L);
    }

    @Test
    void shouldReturnEmptyOptionalIfNoMatchedFormat() {
        final CompositeDateTimeParser parser = new CompositeDateTimeParser(
                new ZonedDateTimeParser(),
                new LocalDateTimeParser(ZoneOffset.UTC)
        );

        final Optional<Long> parsed = parser.getEpochMilli("2017-10-05T10:36:22UTC");

        assertThat(parsed)
                .isEmpty();

    }
}
