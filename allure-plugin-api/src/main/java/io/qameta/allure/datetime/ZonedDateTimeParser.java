package io.qameta.allure.datetime;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

/**
 * @author charlie (Dmitry Baev).
 */
public class ZonedDateTimeParser implements DateTimeParser {

    @Override
    public Optional<Long> getEpochMilli(final String time) {
        final ZonedDateTime parsed = ZonedDateTime.parse(time, ISO_ZONED_DATE_TIME);
        return Optional.of(parsed.toInstant().toEpochMilli());
    }

}
