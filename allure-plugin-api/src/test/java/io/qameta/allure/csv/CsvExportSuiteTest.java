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

import io.qameta.allure.Description;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class CsvExportSuiteTest {

    /**
     * Verifies the success rate is calculated from the history statistic.
     * The test checks the rate is floored to two decimals like in the history tab.
     */
    @Description
    @Test
    void shouldExportSuccessRateFromHistoryStatistic() {
        final Statistic statistic = new Statistic()
                .setPassed(2)
                .setFailed(1);

        final CsvExportSuite suite = new CsvExportSuite(new TestResult().setName("test"), statistic);

        assertThat(suite.getSuccessRate())
                .isEqualTo("66.66%");
    }

    /**
     * Verifies whole success rates are exported without trailing zeros.
     * The test checks a fully passed history is exported as 100%.
     */
    @Description
    @Test
    void shouldExportWholeSuccessRate() {
        final Statistic statistic = new Statistic()
                .setPassed(4);

        final CsvExportSuite suite = new CsvExportSuite(new TestResult().setName("test"), statistic);

        assertThat(suite.getSuccessRate())
                .isEqualTo("100%");
    }

    /**
     * Verifies the success rate is empty when there is no history.
     * The test checks both a missing and an empty history statistic.
     */
    @Description
    @Test
    void shouldNotExportSuccessRateWithoutHistory() {
        assertThat(new CsvExportSuite(new TestResult().setName("test")).getSuccessRate())
                .isNull();
        assertThat(new CsvExportSuite(new TestResult().setName("test"), new Statistic()).getSuccessRate())
                .isNull();
    }

    /**
     * Verifies only issue links are exported to the issues column.
     * The test checks names and urls of issue links are exported and other links are skipped.
     */
    @Description
    @Test
    void shouldExportIssueLinks() {
        final TestResult result = new TestResult()
                .setName("test")
                .setLinks(
                        asList(
                                new Link().setType("issue").setName("ABC-1").setUrl("https://example.com/browse/ABC-1"),
                                new Link().setType("tms").setName("TMS-1").setUrl("https://example.com/tms/TMS-1"),
                                new Link().setType("issue").setName("ABC-2"),
                                new Link().setType("issue").setUrl("https://example.com/browse/ABC-3")
                        )
                );

        final CsvExportSuite suite = new CsvExportSuite(result);

        assertThat(suite.getIssues())
                .isEqualTo("ABC-1 (https://example.com/browse/ABC-1), ABC-2, https://example.com/browse/ABC-3");
    }

    /**
     * Verifies the issues column is empty when there are no issue links.
     * The test checks a result with only non-issue links.
     */
    @Description
    @Test
    void shouldNotExportIssuesWithoutIssueLinks() {
        final TestResult result = new TestResult()
                .setName("test")
                .setLinks(asList(new Link().setType("link").setName("docs").setUrl("https://example.com")));

        assertThat(new CsvExportSuite(result).getIssues())
                .isNull();
    }
}
