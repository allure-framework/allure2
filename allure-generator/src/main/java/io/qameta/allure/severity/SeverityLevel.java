package io.qameta.allure.severity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public enum SeverityLevel implements Serializable {

    BLOCKER("blocker"),
    CRITICAL("critical"),
    NORMAL("normal"),
    MINOR("minor"),
    TRIVIAL("trivial");

    private static final long serialVersionUID = 1L;

    private final String value;

    SeverityLevel(final String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public static Optional<SeverityLevel> fromValue(final String value) {
        return Stream.of(values())
                .filter(level -> level.value().equalsIgnoreCase(value))
                .findFirst();
    }
}
