package io.qameta.allure.utils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public final class ListUtils {

    private ListUtils() {
        throw new IllegalStateException("Do not instance");
    }

    public static <T> T computeIfAbsent(final List<T> list,
                                        final Predicate<T> predicate,
                                        final Supplier<T> defaultValue) {
        Optional<T> any = list.stream().filter(predicate).findAny();
        if (any.isPresent()) {
            return any.get();
        }
        T value = defaultValue.get();
        list.add(value);
        return value;
    }

    public static <T, S> Predicate<T> compareBy(final Function<T, S> map, final Supplier<S> compareWith) {
        return item -> Objects.nonNull(item) && Objects.equals(map.apply(item), compareWith.get());
    }

    @SafeVarargs
    public static <T> T firstNonNull(final T... items) {
        return Stream.of(items)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "firstNonNull method should have at least one non null parameter"
                ));
    }
}
