package io.qameta.allure.datetime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
@RunWith(Parameterized.class)
public class ZonedDateTimeParserTest {

    @Parameterized.Parameter
    public String time;

    @Parameterized.Parameter(1)
    public Long expected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{"2018-05-31T14:05:25.155Z", 1527775525155L},
                new Object[]{"2018-05-06T07:41:51Z", 1525592511000L},
                new Object[]{"2018-05-31T14:05:25.155Z[America/Los_Angeles]", 1527775525155L + TimeUnit.HOURS.toMillis(7)}
        );
    }

    @Test
    public void shouldParseZonedDateTime() {
        final Optional<Long> parsed = new ZonedDateTimeParser().getEpochMilli(time);

        assertThat(parsed)
                .hasValue(expected);
    }

}