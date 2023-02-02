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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class CompositeDateTimeParser implements DateTimeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeDateTimeParser.class);

    private final List<DateTimeParser> parsers;

    public CompositeDateTimeParser(final DateTimeParser... parsers) {
        this(Arrays.asList(parsers));
    }

    public CompositeDateTimeParser(final List<DateTimeParser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public Optional<Long> getEpochMilli(final String time) {
        for (final DateTimeParser parser : parsers) {
            try {
                return parser.getEpochMilli(time);
            } catch (Exception e) {
                LOGGER.debug("Could not parse time {} using parser {}", time, parser, e);
            }
        }
        return Optional.empty();
    }
}
