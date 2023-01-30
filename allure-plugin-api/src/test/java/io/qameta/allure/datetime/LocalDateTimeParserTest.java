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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class LocalDateTimeParserTest {

    @Test
    void shouldParseLocalDateTime() {
        final Optional<Long> parsed = new LocalDateTimeParser(ZoneOffset.UTC)
                .getEpochMilli("2017-10-05T10:36:22");

        assertThat(parsed)
                .hasValue(1507199782000L);
    }

    @Test 
    void shouldParseLocalDateTimeWithNanoseconds() { 
        final Optional<Long> parsed = new LocalDateTimeParser(ZoneOffset.UTC).getEpochMilli("2019-09-24T01:19:42.578340"); 
        assertThat(parsed).hasValue(1569287982578L); 
    } 
 
    @Test
    void shouldChangeZone() {
        final ZoneId pst = ZoneId.of(ZoneId.SHORT_IDS.get("PST"));

        final Optional<Long> parsed = new LocalDateTimeParser(pst)
                .getEpochMilli("2017-10-05T10:36:22");

        assertThat(parsed)
                .hasValue(1507199782000L + TimeUnit.HOURS.toMillis(7));
    }
}
