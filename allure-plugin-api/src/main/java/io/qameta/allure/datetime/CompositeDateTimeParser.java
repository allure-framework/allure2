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
