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
package io.qameta.allure.behaviors;

import io.qameta.allure.Allure;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeNode;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.EPIC;
import static io.qameta.allure.entity.LabelName.FEATURE;
import static io.qameta.allure.entity.LabelName.STORY;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
class BehaviorsPluginTest {

    /**
     * Verifies behavior widget statistics are aggregated by feature and story.
     * The test checks the failed and passed counts for each feature.
     */
    @SuppressWarnings("unchecked")
    @Description
    @Test
    void storiesPerFeatureResultsAggregation() {
        final Set<TestResult> testResults = new HashSet<>();
        testResults.add(
                new TestResult()
                        .setStatus(Status.PASSED)
                        .setLabels(asList(FEATURE.label("feature1"), FEATURE.label("feature2"), STORY.label("story1"), STORY.label("story2")))
        );
        testResults.add(
                new TestResult()
                        .setStatus(Status.FAILED)
                        .setLabels(asList(FEATURE.label("feature2"), FEATURE.label("feature3"), STORY.label("story2"), STORY.label("story3")))
        );

        final LaunchResults results = createLaunchResults(testResults);
        final TreeWidgetData behaviorsData = aggregateBehaviorWidget(results);

        assertThat(behaviorsData.getItems())
                .filteredOn(node2 -> node2.getName().equals("feature1"))
                .extracting(TreeWidgetItem::getStatistic)
                .extracting(Statistic::getFailed, Statistic::getPassed)
                .containsExactly(Tuple.tuple(0L, 2L));

        assertThat(behaviorsData.getItems())
                .filteredOn(node1 -> node1.getName().equals("feature2"))
                .extracting(TreeWidgetItem::getStatistic)
                .extracting(Statistic::getFailed, Statistic::getPassed)
                .containsExactly(Tuple.tuple(2L, 1L));

        assertThat(behaviorsData.getItems())
                .filteredOn(node -> node.getName().equals("feature3"))
                .extracting(TreeWidgetItem::getStatistic)
                .extracting(Statistic::getFailed, Statistic::getPassed)
                .containsExactly(Tuple.tuple(2L, 0L));
    }

    /**
     * Verifies behavior widget aggregation groups results by epic when epics are present.
     * The test checks the top-level widget item names.
     */
    @Description
    @Test
    void shouldGroupByEpic() {
        final Set<TestResult> testResults = new HashSet<>();
        testResults.add(
                new TestResult()
                        .setStatus(Status.PASSED)
                        .setLabels(asList(EPIC.label("e1"), FEATURE.label("f1"), STORY.label("s1")))
        );
        testResults.add(
                new TestResult()
                        .setStatus(Status.FAILED)
                        .setLabels(asList(EPIC.label("e2"), FEATURE.label("f2"), STORY.label("s2")))
        );

        final LaunchResults results = createLaunchResults(testResults);
        final TreeWidgetData behaviorsData = aggregateBehaviorWidget(results);

        assertThat(behaviorsData.getItems())
                .extracting("name")
                .containsExactlyInAnyOrder("e1", "e2");
    }

    /**
     * Verifies behavior tree nodes are sorted by result start time.
     * The test checks timeless results appear first, followed by ascending start times.
     */
    @Issue("587")
    @Issue("572")
    @Description
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

        final LaunchResults results = createLaunchResults(new HashSet<>(Arrays.asList(first, second, timeless)));
        final Tree<TestResult> tree = aggregateBehaviorTree(results);

        assertThat(tree.getChildren())
                .extracting(TreeNode::getName)
                .containsExactly("timeless", "first", "second");
    }

    private LaunchResults createLaunchResults(final Set<TestResult> testResults) {
        return Allure.step("Create launch results", () -> {
            Allure.addAttachment("input-test-results.txt", "text/plain", describeTestResults(testResults));
            return new DefaultLaunchResults(testResults, Collections.emptyMap(), Collections.emptyMap());
        });
    }

    private TreeWidgetData aggregateBehaviorWidget(final LaunchResults results) {
        return Allure.step("Aggregate behavior widget data", () -> {
            final TreeWidgetData data = new BehaviorsPlugin.WidgetAggregator().getData(Collections.singletonList(results));
            Allure.addAttachment("behavior-widget.txt", "text/plain", describeWidgetItems(data.getItems()));
            return data;
        });
    }

    private Tree<TestResult> aggregateBehaviorTree(final LaunchResults results) {
        return Allure.step("Aggregate behavior tree", () -> {
            final Tree<TestResult> tree = BehaviorsPlugin.getData(Collections.singletonList(results));
            Allure.addAttachment("behavior-tree.txt", "text/plain", describeTree(tree.getChildren(), 0));
            return tree;
        });
    }

    private String describeTestResults(final Set<TestResult> testResults) {
        return testResults.stream()
                .map(
                        result -> String.format(
                                "name=%s, status=%s, start=%s, labels=%s",
                                result.getName(),
                                result.getStatus(),
                                result.getTime() == null ? null : result.getTime().getStart(),
                                describeLabels(result)
                        )
                )
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeLabels(final TestResult result) {
        return result.getLabels().stream()
                .map(label -> label.getName() + "=" + label.getValue())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private String describeWidgetItems(final List<TreeWidgetItem> items) {
        return items.stream()
                .map(
                        item -> String.format(
                                "name=%s, failed=%s, passed=%s",
                                item.getName(),
                                item.getStatistic().getFailed(),
                                item.getStatistic().getPassed()
                        )
                )
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeTree(final List<TreeNode> nodes, final int depth) {
        final StringBuilder builder = new StringBuilder();
        for (TreeNode node : nodes) {
            builder
                    .append("  ".repeat(depth))
                    .append(node.getName())
                    .append(System.lineSeparator());
            if (node instanceof TreeGroup) {
                builder.append(describeTree(((TreeGroup) node).getChildren(), depth + 1));
            }
        }
        return builder.toString();
    }
}
