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
package io.qameta.allure.category;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.InMemoryReportStorage;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.qameta.allure.category.CategoriesPlugin.BROKEN_TESTS;
import static io.qameta.allure.category.CategoriesPlugin.CATEGORIES;
import static io.qameta.allure.category.CategoriesPlugin.CSV_FILE_NAME;
import static io.qameta.allure.category.CategoriesPlugin.FAILED_TESTS;
import static io.qameta.allure.category.CategoriesPlugin.JSON_FILE_NAME;
import static io.qameta.allure.category.CategoriesPlugin.stripAnsi;
import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * eroshenkoam
 * 20.04.17
 */
class CategoriesPluginTest {

    private static final String CATEGORY_NAME = "Category";

    /**
     * Verifies defaulting categories to results for category aggregation.
     */
    @Description
    @Test
    void shouldDefaultCategoriesToResults() {
        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.FAILED)
                .setStatusMessage("A");
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.BROKEN)
                .setStatusMessage("B");

        CategoriesPlugin.addCategoriesForResults(createSingleLaunchResults(first, second));

        assertThat(first.getExtraBlock(CATEGORIES, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(FAILED_TESTS.getName());

        assertThat(second.getExtraBlock(CATEGORIES, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(BROKEN_TESTS.getName());

    }

    /**
     * Verifies setting custom categories to results for category aggregation.
     */
    @Description
    @Test
    void shouldSetCustomCategoriesToResults() {
        final String categoryName = "Some category";
        Category category = new Category()
                .setName(categoryName)
                .setMessageRegex(".*")
                .setMatchedStatuses(singletonList(Status.BROKEN));

        Map<String, Object> meta = new HashMap<>();
        meta.put("categories", singletonList(category));

        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.FAILED)
                .setStatusMessage("B");
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.BROKEN)
                .setStatusMessage("B");

        CategoriesPlugin.addCategoriesForResults(createSingleLaunchResults(meta, first, second));

        assertThat(first.getExtraBlock(CATEGORIES, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(FAILED_TESTS.getName());

        assertThat(second.getExtraBlock(CATEGORIES, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(categoryName);
    }

    /**
     * Verifies creating tree for category aggregation.
     */
    @Description
    @Test
    void shouldCreateTree() {
        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.BROKEN)
                .setStatusMessage("M1");
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.FAILED)
                .setStatusMessage("M2");
        final TestResult third = new TestResult()
                .setName("third")
                .setStatus(Status.BROKEN)
                .setStatusMessage("M3");
        final TestResult other = new TestResult()
                .setName("other")
                .setStatus(Status.PASSED)
                .setStatusMessage("M4");

        first.addExtraBlock(CATEGORIES, singletonList(new Category().setName("C1")));
        second.addExtraBlock(CATEGORIES, singletonList(new Category().setName("C2")));
        third.addExtraBlock(CATEGORIES, singletonList(new Category().setName("C1")));
        other.addExtraBlock(CATEGORIES, singletonList(new Category().setName("C3")));

        final List<LaunchResults> launchResults = createSingleLaunchResults(first, second, third, other);
        final Tree<TestResult> tree = CategoriesPlugin.getData(launchResults);

        assertThat(tree.getChildren())
                .hasSize(3)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("C1", "C2", "C3");

        assertThat(tree.getChildren())
                .filteredOn("name", "C1")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("M1", "M3");

        assertThat(tree.getChildren())
                .filteredOn("name", "C1")
                .flatExtracting("children")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first", "third");
    }

    /**
     * Verifies the main category aggregation workflow.
     */
    @Description
    @Test
    void shouldWork() {
        final Configuration configuration = ConfigurationBuilder.bundled().build();

        Category category = new Category()
                .setName(CATEGORY_NAME)
                .setMessageRegex(".*")
                .setMatchedStatuses(singletonList(Status.BROKEN));

        Map<String, Object> meta = new HashMap<>();
        meta.put("categories", singletonList(category));

        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                meta, createTestResult("asd\n", Status.BROKEN)
        );

        CategoriesPlugin plugin = new CategoriesPlugin();

        final InMemoryReportStorage storage = new InMemoryReportStorage();
        aggregateCategories(plugin, configuration, launchResultsList, storage);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();
        List<Category> categories = results.toArray(new TestResult[]{})[0]
                .getExtraBlock("categories");

        assertThat(categories).as("test categories")
                .extracting(Category::getName)
                .containsExactly(category.getName());

        assertThat(storage.getReportDataFiles())
                .containsKey("data/" + JSON_FILE_NAME);

        assertThat(storage.getReportDataFiles())
                .containsKey("data/" + CSV_FILE_NAME);
    }

    /**
     * Verifies custom categories can match flaky test results.
     */
    @Description
    @Test
    void flakyTestsCanBeAddedToCategory() {
        final Configuration configuration = ConfigurationBuilder.bundled().build();

        Category category = new Category()
                .setName(CATEGORY_NAME)
                .setMatchedStatuses(singletonList(Status.FAILED))
                .setFlaky(true);

        Map<String, Object> meta = new HashMap<>();
        meta.put("categories", singletonList(category));

        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                meta, createTestResult("asd\n", Status.FAILED, true)
        );

        CategoriesPlugin plugin = new CategoriesPlugin();

        final InMemoryReportStorage storage = new InMemoryReportStorage();
        aggregateCategories(plugin, configuration, launchResultsList, storage);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();
        List<Category> categories = results.toArray(new TestResult[]{})[0]
                .getExtraBlock("categories");

        assertThat(categories).as("test categories")
                .extracting(Category::getName)
                .containsExactly(category.getName());

        assertThat(storage.getReportDataFiles())
                .containsKey("data/" + JSON_FILE_NAME);
    }

    /**
     * Verifies default category matching includes flaky test results.
     */
    @Description
    @Test
    void flakyTestsShouldBeMatchedByDefault() {
        final Configuration configuration = ConfigurationBuilder.bundled().build();

        final Category category = new Category()
                .setName(CATEGORY_NAME)
                .setMatchedStatuses(singletonList(Status.FAILED));

        final Map<String, Object> meta = new HashMap<>();
        meta.put("categories", singletonList(category));

        final List<LaunchResults> launchResultsList = createSingleLaunchResults(
                meta, createTestResult("asd\n", Status.FAILED, true)
        );

        final CategoriesPlugin plugin = new CategoriesPlugin();

        final InMemoryReportStorage storage = new InMemoryReportStorage();
        aggregateCategories(plugin, configuration, launchResultsList, storage);

        final Set<TestResult> results = launchResultsList.get(0).getAllResults();
        List<Category> categories = results.toArray(new TestResult[]{})[0]
                .getExtraBlock("categories");

        assertThat(categories).as("test categories")
                .extracting(Category::getName)
                .containsExactly(category.getName());

        assertThat(storage.getReportDataFiles())
                .containsKey("data/" + JSON_FILE_NAME);
    }

    /**
     * Verifies sorting category results by ascending start time.
     */
    @Description
    @Issue("587")
    @Issue("572")
    @Test
    void shouldSortByStartTimeAsc() {
        final Category category = new Category().setName("some");

        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.FAILED)
                .setTime(new Time().setStart(10L));
        first.addExtraBlock(CATEGORIES, singletonList(category));
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.FAILED)
                .setTime(new Time().setStart(12L));
        second.addExtraBlock(CATEGORIES, singletonList(category));
        final TestResult timeless = new TestResult()
                .setName("timeless")
                .setStatus(Status.FAILED);
        timeless.addExtraBlock(CATEGORIES, singletonList(category));

        final Tree<TestResult> tree = CategoriesPlugin.getData(
                createSingleLaunchResults(second, first, timeless)
        );

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .flatExtracting("children")
                .extracting("name")
                .containsExactly("timeless", "first", "second");
    }

    private TestResult createTestResult(final String message, final Status status) {
        return createTestResult(message, status, false);
    }

    private TestResult createTestResult(final String message, final Status status, final boolean flaky) {
        return new TestResult()
                .setStatus(status)
                .setStatusMessage(message)
                .setFlaky(flaky);
    }

    /**
     * Verifies removing simple ANSI code for category aggregation.
     */
    @Description
    @Test
    void shouldRemoveSimpleAnsiCode() {
        String input = "\u001B[31mAnsi text\u001B[0m";
        String expected = "Ansi text";
        assertAnsiStripped(input, expected);
    }

    /**
     * Verifies removing multiple ANSI codes for category aggregation.
     */
    @Description
    @Test
    void shouldRemoveMultipleAnsiCodes() {
        String input = "[31mTimed out 5000ms waiting for [39m[2mexpect([22m[31mlocator[39m[2m).[22mtoBeVisible[2m()[22m";
        String expected = "Timed out 5000ms waiting for expect(locator).toBeVisible()";
        assertAnsiStripped(input, expected);
    }

    /**
     * Verifies leaving clean category text unchanged when no ANSI codes are present.
     */
    @Description
    @Test
    void shouldReturnUnchangedIfNoAnsi() {
        String input = "Clean text";
        assertAnsiStripped(input, "Clean text");
    }

    /**
     * Verifies sanitizing category description HTML for category aggregation.
     */
    @Description
    @Test
    void shouldSanitizeCategoryDescriptionHtml(@TempDir final Path directory) throws IOException {
        final String categoriesJson = "[{\"name\":\"xss\",\"descriptionHtml\":\"<script>alert(1)</script><p>safe</p>\"}]";
        Files.writeString(directory.resolve(JSON_FILE_NAME), categoriesJson);

        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final DefaultResultsVisitor visitor = new DefaultResultsVisitor(configuration);
        final CategoriesPlugin plugin = new CategoriesPlugin();
        Allure.step(
                "Read category definitions from " + directory,
                () -> plugin.readResults(configuration, visitor, directory)
        );

        final LaunchResults launchResults = visitor.getLaunchResults();
        final List<Category> categories = launchResults.getExtra(CATEGORIES, ArrayList::new);
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getDescriptionHtml())
                .contains("<p>safe</p>")
                .doesNotContain("<script")
                .doesNotContain("alert(");
    }

    private void aggregateCategories(
                                     final CategoriesPlugin plugin,
                                     final Configuration configuration,
                                     final List<LaunchResults> launchResults,
                                     final InMemoryReportStorage storage) {
        Allure.step("Aggregate categories for " + launchResults.size() + " launch(es)", () -> {
            plugin.aggregate(configuration, launchResults, storage);
            attachStorageFiles(storage);
        });
    }

    private void assertAnsiStripped(final String input, final String expected) {
        Allure.step("Strip ANSI escapes from category text", () -> {
            final String actual = stripAnsi(input);
            Allure.addAttachment(
                    "ANSI stripping sample",
                    "text/plain",
                    String.format("input=%s%nexpected=%s%nactual=%s%n", input, expected, actual)
            );
            assertThat(actual).isEqualTo(expected);
        });
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
}
