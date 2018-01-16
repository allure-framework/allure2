package io.qameta.allure.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author charlie (Dmitry Baev).
 */
public final class ConvertUtils {

    private ConvertUtils() {
        throw new IllegalStateException("Do not instance");
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

    @SafeVarargs
    public static <T> Optional<T> firstNonNullSafe(final T... items) {
        return Stream.of(items)
                .filter(Objects::nonNull)
                .findFirst();
    }

    public static <T, R> List<R> convertList(final Collection<T> source,
                                             final Function<T, R> converter) {
        return isNull(source) ? new ArrayList<>() : source.stream()
                .map(converter)
                .collect(toList());
    }

    public static <T, R> Set<R> convertSet(final Collection<T> source,
                                           final Function<T, R> converter) {
        return isNull(source) ? new HashSet<>() : source.stream()
                .map(converter)
                .collect(toSet());
    }

    public static <T, R> Set<R> convertSet(final Collection<T> source,
                                           final Predicate<T> predicate,
                                           final Function<T, R> converter) {
        return isNull(source) ? new HashSet<>() : source.stream()
                .filter(predicate)
                .map(converter)
                .collect(toSet());
    }
}
