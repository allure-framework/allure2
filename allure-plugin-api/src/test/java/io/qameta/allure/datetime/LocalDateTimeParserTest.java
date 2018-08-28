package io.qameta.allure.datetime;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class LocalDateTimeParserTest {

    @Test
    public void shouldParseLocalDateTime() {
        final Optional<Long> parsed = new LocalDateTimeParser(ZoneOffset.UTC)
                .getEpochMilli("2017-10-05T10:36:22");

        assertThat(parsed)
                .hasValue(1507199782000L);
    }

    @Test
    public void shouldChangeZone() {
        final ZoneId pst = ZoneId.of(ZoneId.SHORT_IDS.get("PST"));

        final Optional<Long> parsed = new LocalDateTimeParser(pst)
                .getEpochMilli("2017-10-05T10:36:22");

        assertThat(parsed)
                .hasValue(1507199782000L + TimeUnit.HOURS.toMillis(7));
    }
}