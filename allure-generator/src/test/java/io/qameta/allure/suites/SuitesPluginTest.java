/*
 *  Copyright 2016-2024 Qameta Software Inc
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
package io.qameta.allure.suites;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Issue;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.InMemoryReportStorage;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class SuitesPluginTest {

    @Test
    void shouldCreateTree() {

        final Tree<TestResult> tree = SuitesPlugin.getData(getSimpleLaunchResults());

        assertThat(tree.getChildren())
                .hasSize(2)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("s1", "s2");
    }

    @Issue("587")
    @Issue("572")
    @Test
    void shouldSortByStartTimeAsc() {
        final TestResult first = new TestResult()
                .setName("first")
                .setTime(new Time().setStart(10L));
        final TestResult second = new TestResult()
                .setName("second")
                .setTime(new Time().setStart(12L));
        final TestResult timeless = new TestResult()
                .setName("timeless");

        final Tree<TestResult> tree = SuitesPlugin.getData(
                createSingleLaunchResults(second, first, timeless)
        );

        assertThat(tree.getChildren())
                .extracting(TreeNode::getName)
                .containsExactly("timeless", "first", "second");
    }

    @Test
    void shouldCreateCsvFile() {
        final Configuration configuration = ConfigurationBuilder.bundled().build();

        final SuitesPlugin plugin = new SuitesPlugin();

        final InMemoryReportStorage storage = new InMemoryReportStorage();
        plugin.aggregate(configuration, getSimpleLaunchResults(), storage);

        assertThat(storage.getReportDataFiles())
                .containsKey("data/" + SuitesPlugin.JSON_FILE_NAME);

        assertThat(storage.getReportDataFiles())
                .containsKey("data/" + SuitesPlugin.CSV_FILE_NAME);
    }

    private List<LaunchResults> getSimpleLaunchResults() {
        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(singletonList(new Label().setName("suite").setValue("s1")));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(singletonList(new Label().setName("suite").setValue("s1")));
        final TestResult third = new TestResult()
                .setName("third")
                .setLabels(singletonList(new Label().setName("suite").setValue("s2")));
        return createSingleLaunchResults(second, first, third);
    }
}
