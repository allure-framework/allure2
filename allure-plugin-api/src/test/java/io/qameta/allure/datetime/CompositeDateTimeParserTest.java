package io.qameta.allure.datetime;

import org.junit.Test;

import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class CompositeDateTimeParserTest {

    @Test
    public void shouldReturnFirstParsed() {
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
    public void shouldReturnEmptyOptionalIfNoMatchedFormat() {
        final CompositeDateTimeParser parser = new CompositeDateTimeParser(
                new ZonedDateTimeParser(),
                new LocalDateTimeParser(ZoneOffset.UTC)
        );

        final Optional<Long> parsed = parser.getEpochMilli("2017-10-05T10:36:22UTC");

        assertThat(parsed)
                .isEmpty();

    }
}