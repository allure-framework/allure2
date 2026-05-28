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
package io.qameta.allure.labels;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Description;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.InMemoryReportStorage;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static org.assertj.core.api.Assertions.assertThat;

class LabelTabsPluginTest {

    @Description
    @Test
    void shouldCreateTagTree() {
        final Tree<TestResult> tree = Allure.step(
                "Build tag tree from launch results",
                () -> LabelTabsPlugin.getData(
                        getLaunchResults(),
                        LabelTabsPlugin.TAGS,
                        LabelName.TAG
                )
        );

        assertThat(tree.getChildren())
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("api", "smoke");
    }

    @Description
    @Test
    void shouldCreateIssueTree() {
        final Tree<TestResult> tree = Allure.step(
                "Build issue tree from launch results",
                () -> LabelTabsPlugin.getData(
                        getLaunchResults(),
                        LabelTabsPlugin.ISSUES,
                        LabelName.ISSUE
                )
        );

        assertThat(tree.getChildren())
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("AUTH-1", "BILL-2");
    }

    @Description
    @Test
    void shouldCreateReportDataFiles() {
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final LabelTabsPlugin plugin = new LabelTabsPlugin();
        final InMemoryReportStorage storage = new InMemoryReportStorage();

        Allure.step("Aggregate label tab report files", () -> {
            plugin.aggregate(configuration, getLaunchResults(), storage);
            attachStorageFiles(storage);
        });

        assertThat(storage.getReportDataFiles())
                .containsKeys(
                        "data/tags.json",
                        "data/issues.json",
                        "data/test-types.json"
                );
    }

    private void attachStorageFiles(final InMemoryReportStorage storage) {
        Allure.step(
                "Attach in-memory storage contents", () -> storage.getReportDataFiles().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(
                                entry -> Allure.addAttachment(
                                        entry.getKey(),
                                        "text/plain",
                                        new String(
                                                Base64.getDecoder().decode(entry.getValue()),
                                                StandardCharsets.UTF_8
                                        )
                                )
                        )
        );
    }

    private List<LaunchResults> getLaunchResults() {
        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(
                        Arrays.asList(
                                new Label().setName("tag").setValue("smoke"),
                                new Label().setName("issue").setValue("AUTH-1"),
                                new Label().setName("testType").setValue("api")
                        )
                );
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(
                        Arrays.asList(
                                new Label().setName("tag").setValue("api"),
                                new Label().setName("issue").setValue("BILL-2"),
                                new Label().setName("testType").setValue("ui")
                        )
                );
        final TestResult third = new TestResult()
                .setName("third")
                .setLabels(Arrays.asList(new Label().setName("tag").setValue("smoke")));
        return createSingleLaunchResults(second, first, third);
    }
}
