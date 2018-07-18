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
