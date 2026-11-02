/*
 *  Copyright 2016-2026 Qameta Software Inc
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
package io.qameta.allure.summary;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Report-level summary shared with Allure 3 CI integrations.
 */
@Data
@Accessors(chain = true)
public class ReportSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected Statistic stats;
    protected Status status;
    protected long duration;
    protected long createdAt;
    protected String plugin = "Allure2";
    protected Map<String, Object> meta = new HashMap<>();

    public static ReportSummary create(final Configuration configuration,
                                       final List<LaunchResults> launches,
                                       final boolean singleFile) {
        final SummaryData widget = SummaryPlugin.getData(configuration, launches);
        final Statistic stats = widget.getStatistic();
        final ReportSummary summary = new ReportSummary()
                .setName(widget.getReportName())
                .setStats(stats)
                .setStatus(stats.getTotal() == 0 ? Status.PASSED : stats.getStatus());
        summary.getMeta().put("reportId", configuration.getUuid());
        summary.getMeta().put("singleFile", singleFile);

        final List<Time> times = launches.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .map(TestResult::getTime)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        summary.updateTime(times);
        return summary;
    }

    private void updateTime(final List<Time> times) {
        long start = Long.MAX_VALUE;
        long stop = Long.MIN_VALUE;
        long sumDuration = 0;
        boolean hasTimestamps = false;
        for (Time time : times) {
            if (Objects.nonNull(time.getStart()) && Objects.nonNull(time.getStop())) {
                hasTimestamps = true;
                start = Math.min(start, time.getStart());
                stop = Math.max(stop, time.getStop());
            }
            if (Objects.nonNull(time.getDuration())) {
                sumDuration += time.getDuration();
            }
            if (Objects.nonNull(time.getStop())) {
                createdAt = Math.max(createdAt, time.getStop());
            }
        }
        duration = hasTimestamps ? Math.max(0, stop - start) : sumDuration;
    }
}
