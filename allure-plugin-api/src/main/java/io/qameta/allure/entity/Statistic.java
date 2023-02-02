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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class Statistic implements Serializable {

    private static final long serialVersionUID = 1L;

    protected long failed;
    protected long broken;
    protected long skipped;
    protected long passed;
    protected long unknown;

    @JsonProperty
    public long getTotal() {
        return getFailed() + getBroken() + getPassed() + getSkipped() + getUnknown();
    }

    /**
     * To ignore total property during deserialization. Should not be used manually.
     *
     * @deprecated Do not use it manually.
     */
    @Deprecated
    @JsonProperty
    public void setTotal(final long total) {
        //do nothing
    }

    public long get(final Status status) {
        switch (status) {
            case FAILED:
                return getFailed();
            case BROKEN:
                return getBroken();
            case PASSED:
                return getPassed();
            case SKIPPED:
                return getSkipped();
            default:
                return getUnknown();
        }
    }

    @JsonIgnore
    public Status getStatus() {
        for (final Status status : Status.values()) {
            if (get(status) > 0) {
                return status;
            }
        }
        return Status.UNKNOWN;
    }

    public void update(final Statusable statusable) {
        if (Objects.isNull(statusable)) {
            return;
        }
        update(statusable.getStatus());
    }

    public void update(final Status status) {
        if (Objects.isNull(status)) {
            return;
        }
        switch (status) {
            case FAILED:
                setFailed(getFailed() + 1);
                break;
            case BROKEN:
                setBroken(getBroken() + 1);
                break;
            case PASSED:
                setPassed(getPassed() + 1);
                break;
            case SKIPPED:
                setSkipped(getSkipped() + 1);
                break;
            default:
                setUnknown(getUnknown() + 1);
                break;
        }
    }

    public void merge(final Statistic other) {
        if (Objects.isNull(other)) {
            return;
        }
        setFailed(getFailed() + other.getFailed());
        setBroken(getBroken() + other.getBroken());
        setPassed(getPassed() + other.getPassed());
        setSkipped(getSkipped() + other.getSkipped());
        setUnknown(getUnknown() + other.getUnknown());
    }

    public static Comparator<Statistic> comparator() {
        return Comparator.comparing(Statistic::getFailed)
                .thenComparing(Statistic::getBroken)
                .thenComparing(Statistic::getPassed)
                .thenComparing(Statistic::getSkipped)
                .thenComparing(Statistic::getUnknown);
    }
}
