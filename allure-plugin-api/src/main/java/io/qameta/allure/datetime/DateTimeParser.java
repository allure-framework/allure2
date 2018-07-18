package io.qameta.allure.datetime;

import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface DateTimeParser {

    Optional<Long> getEpochMilli(String time);

}
