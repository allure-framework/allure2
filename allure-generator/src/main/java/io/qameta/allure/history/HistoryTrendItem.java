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
package io.qameta.allure.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.qameta.allure.entity.Statistic;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class HistoryTrendItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("data")
    @XmlElement(required = true)
    protected Statistic data;
    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long buildOrder;
    @XmlElement(required = true)
    protected String reportUrl;
    @XmlElement(required = true)
    protected String reportName;

    @JsonIgnore
    public Statistic getStatistic() {
        return data;
    }

    @JsonSetter("statistic")
    public HistoryTrendItem setStatistic(final Statistic statistic) {
        this.data = statistic;
        return this;
    }
}
