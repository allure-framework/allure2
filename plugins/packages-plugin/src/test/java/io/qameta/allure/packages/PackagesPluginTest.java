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
package io.qameta.allure.packages;

import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.Issue;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.TEST_METHOD;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class PackagesPluginTest {

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

        final LaunchResults results = new DefaultLaunchResults(
                testResults,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        final PackagesPlugin packagesPlugin = new PackagesPlugin();
        final Tree<TestResult> tree = packagesPlugin.getData(singletonList(results));

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

        final LaunchResults results = new DefaultLaunchResults(
                testResults,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        final PackagesPlugin packagesPlugin = new PackagesPlugin();
        final Tree<TestResult> tree = packagesPlugin.getData(singletonList(results));

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

    @Issue("531")
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

        final LaunchResults results = new DefaultLaunchResults(
                testResults,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        final PackagesPlugin packagesPlugin = new PackagesPlugin();
        final Tree<TestResult> tree = packagesPlugin.getData(singletonList(results));

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

    @Issue("587")
    @Issue("572")
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

        final LaunchResults results = new DefaultLaunchResults(
                new HashSet<>(Arrays.asList(first, second, third, timeless)),
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        final PackagesPlugin packagesPlugin = new PackagesPlugin();
        final Tree<TestResult> tree = packagesPlugin.getData(singletonList(results));

        assertThat(tree.getChildren())
                .extracting("name")
                .containsExactly("timeless", "first", "third", "second");
    }
}
