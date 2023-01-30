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
package io.qameta.allure.tree;

import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.qameta.allure.entity.LabelName.FEATURE;
import static io.qameta.allure.entity.LabelName.STORY;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class TestResultTreeTest {

    @Test
    void shouldCreateEmptyTree() {
        final Tree<TestResult> tree = new TestResultTree(
                "default",
                item -> Collections.emptyList()
        );

        assertThat(tree.getChildren())
                .hasSize(0);
    }

    @Test
    void shouldCrossGroup() {
        final Tree<TestResult> behaviors = new TestResultTree(
                "behaviors",
                testResult -> groupByLabels(testResult, FEATURE, STORY)
        );

        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(asList(feature("f1"), feature("f2"), story("s1"), story("s2")));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(asList(feature("f2"), feature("f3"), story("s2"), story("s3")));
        behaviors.add(first);
        behaviors.add(second);

        assertThat(behaviors.getChildren())
                .hasSize(3)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("f1", "f2", "f3");

        assertThat(behaviors.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("s1", "s2", "s1", "s2", "s3", "s2", "s3");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("s1", "s2", "s3");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s1")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s2")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first", "second");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s3")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("second");
    }

    private Label feature(final String value) {
        return new Label().setName("feature").setValue(value);
    }

    private Label story(final String value) {
        return new Label().setName("story").setValue(value);
    }

}
