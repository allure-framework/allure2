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
package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static io.qameta.allure.entity.EntityUtils.firstNonNull;
import static java.lang.Long.MAX_VALUE;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class GroupTime implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long start;
    protected Long stop;
    protected Long duration;
    protected Long minDuration;
    protected Long maxDuration;
    protected Long sumDuration;

    public void merge(final GroupTime groupTime) {
        if (Objects.isNull(groupTime)) {
            return;
        }
        update(firstNonNull(getStart(), MAX_VALUE), groupTime.getStart(), Math::min, this::setStart);
        update(firstNonNull(getStop(), 0L), groupTime.getStop(), Math::max, this::setStop);
        update(getStop(), getStart(), (a, b) -> a - b, this::setDuration);
        update(firstNonNull(getMinDuration(), MAX_VALUE), groupTime.getMinDuration(), Math::min, this::setMinDuration);
        update(firstNonNull(getMaxDuration(), 0L), groupTime.getMaxDuration(), Math::max, this::setMaxDuration);
        update(firstNonNull(getSumDuration(), 0L), groupTime.getSumDuration(), (a, b) -> a + b, this::setSumDuration);
    }

    public void update(final Timeable timeable) {
        if (Objects.isNull(timeable)) {
            return;
        }
        update(timeable.getTime());
    }

    public void update(final Time time) {
        if (Objects.isNull(time)) {
            return;
        }
        update(firstNonNull(getStart(), MAX_VALUE), time.getStart(), Math::min, this::setStart);
        update(firstNonNull(getStop(), 0L), time.getStop(), Math::max, this::setStop);
        update(getStop(), getStart(), (a, b) -> a - b, this::setDuration);
        update(firstNonNull(getMinDuration(), MAX_VALUE), time.getDuration(), Math::min, this::setMinDuration);
        update(firstNonNull(getMaxDuration(), 0L), time.getDuration(), Math::max, this::setMaxDuration);
        update(firstNonNull(getSumDuration(), 0L), time.getDuration(), (a, b) -> a + b, this::setSumDuration);
    }

    protected static <T> void update(final T first, final T second,
                                     final BiFunction<T, T, T> merge, final Consumer<T> setter) {
        if (Objects.nonNull(first) && Objects.nonNull(second)) {
            setter.accept(merge.apply(first, second));
        }
    }
}
