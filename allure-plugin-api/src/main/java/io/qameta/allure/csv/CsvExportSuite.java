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
package io.qameta.allure.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.ZoneId.systemDefault;

/**
 * Class contains the information for the suites csv export.
 *
 */
public class CsvExportSuite implements Serializable {

    private static final String ISSUE_LINK_TYPE = "issue";

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 0)
    private final String status;

    @CsvBindByName(column = "Start Time")
    @CsvBindByPosition(position = 1)
    private final String start;

    @CsvBindByName(column = "Stop Time")
    @CsvBindByPosition(position = 2)
    private final String stop;

    @CsvBindByName(column = "Duration in ms")
    @CsvBindByPosition(position = 3)
    private final String duration;

    @CsvBindByName(column = "Parent Suite")
    @CsvBindByPosition(position = 4)
    private final String parentSuite;

    @CsvBindByName(column = "Suite")
    @CsvBindByPosition(position = 5)
    private final String suite;

    @CsvBindByName(column = "Sub Suite")
    @CsvBindByPosition(position = 6)
    private final String subSuite;

    @CsvBindByName(column = "Test Class")
    @CsvBindByPosition(position = 7)
    private final String testClass;

    @CsvBindByName(column = "Test Method")
    @CsvBindByPosition(position = 8)
    private final String testMethod;

    @CsvBindByName(column = "Name")
    @CsvBindByPosition(position = 9)
    private final String name;

    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 10)
    private final String description;

    @CsvBindByName(column = "Success Rate")
    @CsvBindByPosition(position = 11)
    private final String successRate;

    @CsvBindByName(column = "Issues")
    @CsvBindByPosition(position = 12)
    private final String issues;

    public CsvExportSuite(final TestResult result) {
        this(result, null);
    }

    /**
     * Creates the suites csv export row.
     *
     * @param result           the test result.
     * @param historyStatistic the history statistic of the test result, may be null.
     */
    public CsvExportSuite(final TestResult result, final Statistic historyStatistic) {
        final Map<String, String> resultMap = result.toMap();
        this.status = result.getStatus() != null ? result.getStatus().value() : null;
        this.duration = result.getTime().getDuration() != null ? result.getTime().getDuration().toString() : null;
        this.start = result.getTime().getStart() != null ? asDate(result.getTime().getStart()) : null;
        this.stop = result.getTime().getStop() != null ? asDate(result.getTime().getStop()) : null;
        this.parentSuite = resultMap.getOrDefault("parentSuite", null);
        this.suite = resultMap.getOrDefault("suite", null);
        this.subSuite = resultMap.getOrDefault("subSuite", null);
        this.testClass = resultMap.getOrDefault("testClass", null);
        this.testMethod = resultMap.getOrDefault("testMethod", null);
        this.name = result.getName();
        this.description = result.getDescription();
        this.successRate = asSuccessRate(historyStatistic);
        this.issues = asIssues(result.getLinks());
    }

    public String getStatus() {
        return status;
    }

    public String getDuration() {
        return duration;
    }

    public String getStart() {
        return start;
    }

    public String getStop() {
        return stop;
    }

    public String getParentSuite() {
        return parentSuite;
    }

    public String getSuite() {
        return suite;
    }

    public String getSubSuite() {
        return subSuite;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public String getIssues() {
        return issues;
    }

    private static String asIssues(final List<Link> links) {
        if (Objects.isNull(links)) {
            return null;
        }
        final String value = links.stream()
                .filter(link -> ISSUE_LINK_TYPE.equalsIgnoreCase(link.getType()))
                .map(CsvExportSuite::asIssue)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        return value.isEmpty() ? null : value;
    }

    private static String asIssue(final Link link) {
        final String name = link.getName();
        final String url = link.getUrl();
        if (Objects.isNull(url) || url.equals(name)) {
            return name;
        }
        return Objects.isNull(name) ? url : name + " (" + url + ")";
    }

    private static String asSuccessRate(final Statistic statistic) {
        if (Objects.isNull(statistic) || statistic.getTotal() == 0) {
            return null;
        }
        final BigDecimal rate = BigDecimal.valueOf(statistic.getPassed() * 100)
                .divide(BigDecimal.valueOf(statistic.getTotal()), 2, RoundingMode.FLOOR)
                .stripTrailingZeros();
        return rate.toPlainString() + "%";
    }

    private static String asDate(final Long epochMills) {
        if (Objects.nonNull(epochMills)) {
            return Instant.ofEpochMilli(epochMills)
                    .atZone(systemDefault())
                    .toLocalDate().toString();
        }
        return null;
    }
}
