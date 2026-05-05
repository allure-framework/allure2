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
package io.qameta.allure.packages;

import io.qameta.allure.Allure;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.TEST_METHOD;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class PackagesPluginTest {

    /**
     * Verifies package labels are converted into a package tree.
     * The test checks root, package, and leaf node names for labeled results.
     */
    @Description
    @Test
    void shouldCreateTree() {
        final Set<TestResult> testResults = new HashSet<>();

        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(asList(PACKAGE.label("a.b"), TEST_METHOD.label("firstMethod")));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(singletonList(PACKAGE.label("a.c")));
        testResults.add(first);
        testResults.add(second);

        final LaunchResults results = createLaunchResults(testResults);
        final Tree<TestResult> tree = aggregatePackages(results);

        assertThat(tree.getChildren())
                .hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("a");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("b", "c");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("firstMethod", "second");
    }

    /**
     * Verifies package tree nodes with a single child are collapsed.
     * The test checks collapsed package names and leaf result names.
     */
    @Description
    @Test
    void shouldCollapseNodesWithOneChild() {
        final Set<TestResult> testResults = new HashSet<>();

        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(singletonList(PACKAGE.label("a.b.c")));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(singletonList(PACKAGE.label("a.d.e")));
        testResults.add(first);
        testResults.add(second);

        final LaunchResults results = createLaunchResults(testResults);
        final Tree<TestResult> tree = aggregatePackages(results);

        assertThat(tree.getChildren())
                .hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("a");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("b.c", "d.e");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first", "second");
    }

    /**
     * Verifies tests can appear directly under a package that also has nested packages.
     * The test checks the nested package shape for a mixed package fixture.
     */
    @Issue("531")
    @Description
    @Test
    void shouldProcessTestsInNestedPackages() {
        final Set<TestResult> testResults = new HashSet<>();
        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(singletonList(PACKAGE.label("a.b")));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(singletonList(PACKAGE.label("a.b.c")));

        testResults.add(first);
        testResults.add(second);

        final LaunchResults results = createLaunchResults(testResults);
        final Tree<TestResult> tree = aggregatePackages(results);

        assertThat(tree.getChildren())
                .hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("a.b");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first", "c");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .filteredOn("name", "c")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("second");
    }

    /**
     * Verifies package tree nodes are sorted by result start time.
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
                .setTime(new Time().setStart(100L));
        final TestResult third = new TestResult()
                .setName("third")
                .setTime(new Time().setStart(50L));
        final TestResult timeless = new TestResult()
                .setName("timeless");

        final LaunchResults results = createLaunchResults(new HashSet<>(Arrays.asList(first, second, third, timeless)));
        final Tree<TestResult> tree = aggregatePackages(results);

        assertThat(tree.getChildren())
                .extracting("name")
                .containsExactly("timeless", "first", "third", "second");
    }

    private LaunchResults createLaunchResults(final Set<TestResult> testResults) {
        return Allure.step("Create launch results", () -> {
            Allure.addAttachment("input-test-results.txt", "text/plain", describeTestResults(testResults));
            return new DefaultLaunchResults(testResults, Collections.emptyMap(), Collections.emptyMap());
        });
    }

    private Tree<TestResult> aggregatePackages(final LaunchResults results) {
        return Allure.step("Aggregate packages tree", () -> {
            final PackagesPlugin packagesPlugin = new PackagesPlugin();
            final Tree<TestResult> tree = packagesPlugin.getData(singletonList(results));
            Allure.addAttachment("packages-tree.txt", "text/plain", describeTree(tree.getChildren(), 0));
            return tree;
        });
    }

    private String describeTestResults(final Set<TestResult> testResults) {
        return testResults.stream()
                .map(result -> String.format(
                        "name=%s, start=%s, labels=%s",
                        result.getName(),
                        result.getTime() == null ? null : result.getTime().getStart(),
                        describeLabels(result)
                ))
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeLabels(final TestResult result) {
        return result.getLabels().stream()
                .map(label -> label.getName() + "=" + label.getValue())
                .sorted()
                .collect(Collectors.joining(", "));
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
