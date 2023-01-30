/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Useful conversion methods.
 */
public final class ConvertUtils {

    private ConvertUtils() {
        throw new IllegalStateException("do not instance");
    }

    public static <T, R> List<R> convertList(final Collection<T> source, final Function<T, R> converter) {
        return convertList(source, t -> true, converter);
    }

    public static <T, R> List<R> convertList(final Collection<T> source,
                                             final Predicate<T> predicate,
                                             final Function<T, R> converter) {
        return Objects.isNull(source) ? null : source.stream()
                .filter(predicate)
                .map(converter)
                .collect(toList());
    }

}
