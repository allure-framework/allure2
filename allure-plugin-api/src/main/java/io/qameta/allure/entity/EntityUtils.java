package io.qameta.allure.entity;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
/*package private*/ final class EntityUtils {

    private EntityUtils() {
        throw new IllegalStateException("Do not instance");
    }

    @SafeVarargs
    public static <T> T firstNonNull(final T... items) {
        return Stream.of(items)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("At least one argument should be not null"));
    }
}
