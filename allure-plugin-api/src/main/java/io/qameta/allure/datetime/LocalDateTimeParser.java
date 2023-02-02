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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class LocalDateTimeParser implements DateTimeParser {

    private final ZoneId zoneId;

    public LocalDateTimeParser() {
        this(ZoneId.systemDefault());
    }

    public LocalDateTimeParser(final ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public Optional<Long> getEpochMilli(final String time) {
        final LocalDateTime parsed = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Optional.of(parsed.atZone(zoneId).toInstant().toEpochMilli());
    }

}
