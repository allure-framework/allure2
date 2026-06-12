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
package io.qameta.allure.tags;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Description;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.InMemoryReportStorage;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

class TagsTreePluginTest {

    @Description("Verifies that a result is grouped under every distinct tag.")
    @Test
    void shouldGroupResultsByTags() {
        final TestResult result = new TestResult().setName("tagged");
        result.addExtraBlock(TagsPlugin.TAGS_BLOCK_NAME, new HashSet<>(asList("jira-123", "smoke")));
        final TestResult untagged = new TestResult().setName("untagged");

        final Tree<TestResult> tree = Allure.step(
                "Build tag tree from a result with two tags",
                () -> TagsTreePlugin.getTagData(createSingleLaunchResults(result, untagged))
        );

        assertThat(tree.getChildren())
                .extracting(TreeNode::getName)
                .containsExactly("jira-123", "smoke");
    }

    @Description("Verifies that the tags tree data file is generated.")
    @Test
    void shouldCreateTagsDataFile() {
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final InMemoryReportStorage storage = new InMemoryReportStorage();
        final TestResult result = new TestResult().setName("tagged");
        result.addExtraBlock(TagsPlugin.TAGS_BLOCK_NAME, singleton("smoke"));
        final List<LaunchResults> launches = createSingleLaunchResults(result);

        Allure.step("Generate tags tree data", () -> new TagsTreePlugin().aggregate(configuration, launches, storage));

        assertThat(storage.getReportDataFiles())
                .containsKey("data/" + TagsTreePlugin.JSON_FILE_NAME);
    }
}
