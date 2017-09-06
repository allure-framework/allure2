package io.qameta.allure.category;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Issue;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.qameta.allure.category.CategoriesPlugin.BROKEN_TESTS;
import static io.qameta.allure.category.CategoriesPlugin.CATEGORIES_BLOCK_NAME;
import static io.qameta.allure.category.CategoriesPlugin.FAILED_TESTS;
import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * eroshenkoam
 * 20.04.17
 */
public class CategoriesPluginTest {

    private static final String CATEGORY_NAME = "Category";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Configuration configuration;

    private Path reportPath;

    @Before
    public void setUp() throws IOException {
        reportPath = Paths.get(folder.newFolder("report").getAbsolutePath());
        configuration = new ConfigurationBuilder().useDefault().build();
    }

    @Test
    public void shouldDefaultCategoriesToResults() throws Exception {
        final CategoriesPlugin categoriesPlugin = new CategoriesPlugin();

        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.FAILED)
                .setStatusDetails(new StatusDetails().setMessage("A"));
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.BROKEN)
                .setStatusDetails(new StatusDetails().setMessage("B"));

        categoriesPlugin.addCategoriesForResults(createSingleLaunchResults(first, second));

        assertThat(first.getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(FAILED_TESTS.getName());

        assertThat(second.getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(BROKEN_TESTS.getName());

    }

    @Test
    public void shouldSetCustomCategoriesToResults() throws Exception {
        final String categoryName = "Some category";
        Category category = new Category()
                .setName(categoryName)
                .setMessageRegex(".*")
                .setMatchedStatuses(singletonList(Status.BROKEN));

        Map<String, Object> meta = new HashMap<>();
        meta.put("categories", singletonList(category));

        final CategoriesPlugin categoriesPlugin = new CategoriesPlugin();

        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.FAILED)
                .setStatusDetails(new StatusDetails().setMessage("A"));
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.BROKEN)
                .setStatusDetails(new StatusDetails().setMessage("B"));

        categoriesPlugin.addCategoriesForResults(createSingleLaunchResults(meta, first, second));

        assertThat(first.getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(FAILED_TESTS.getName());

        assertThat(second.getExtraBlock(CATEGORIES_BLOCK_NAME, new ArrayList<Category>()))
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder(categoryName);
    }

    @Test
    public void shouldCreateTree() throws Exception {
        final CategoriesPlugin categoriesPlugin = new CategoriesPlugin();

        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.BROKEN)
                .setStatusDetails(new StatusDetails().setMessage("M1"));
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.FAILED)
                .setStatusDetails(new StatusDetails().setMessage("M2"));
        final TestResult third = new TestResult()
                .setName("third")
                .setStatus(Status.BROKEN)
                .setStatusDetails(new StatusDetails().setMessage("M3"));
        final TestResult other = new TestResult()
                .setName("other")
                .setStatus(Status.PASSED)
                .setStatusDetails(new StatusDetails().setMessage("M4"));

        first.setExtraBlock(CATEGORIES_BLOCK_NAME, singletonList(new Category().setName("C1")));
        second.setExtraBlock(CATEGORIES_BLOCK_NAME, singletonList(new Category().setName("C2")));
        third.setExtraBlock(CATEGORIES_BLOCK_NAME, singletonList(new Category().setName("C1")));
        other.setExtraBlock(CATEGORIES_BLOCK_NAME, singletonList(new Category().setName("C3")));

        final List<LaunchResults> launchResults = createSingleLaunchResults(first, second, third, other);
        final Tree<TestResult> tree = categoriesPlugin.getData(launchResults);

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

    @Test
    public void shouldWork() throws IOException {

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

        plugin.aggregate(configuration, launchResultsList, reportPath);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();
        List<Category> categories = results.toArray(new TestResult[]{})[0]
                .getExtraBlock("categories");

        assertThat(categories).as("test categories")
                .extracting(Category::getName)
                .containsExactly(category.getName());

        assertThat(reportPath.resolve("data").resolve("categories.json"))
                .exists();

    }

    @Test
    public void flakyTestsCanBeAddedToCategory() throws IOException {
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

        plugin.aggregate(configuration, launchResultsList, reportPath);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();
        List<Category> categories = results.toArray(new TestResult[]{})[0]
                .getExtraBlock("categories");

        assertThat(categories).as("test categories")
                .extracting(Category::getName)
                .containsExactly(category.getName());

        assertThat(reportPath.resolve("data").resolve("categories.json"))
                .exists();
    }

    @Issue("587")
    @Issue("572")
    @Test
    public void shouldSortByStartTimeAsc() throws Exception {
        final Category category = new Category().setName("some");

        final TestResult first = new TestResult()
                .setName("first")
                .setStatus(Status.FAILED)
                .setTime(new Time().setStart(10L))
                .setExtraBlock(CATEGORIES_BLOCK_NAME, singletonList(category));
        final TestResult second = new TestResult()
                .setName("second")
                .setStatus(Status.FAILED)
                .setTime(new Time().setStart(12L))
                .setExtraBlock(CATEGORIES_BLOCK_NAME, singletonList(category));
        final TestResult timeless = new TestResult()
                .setName("timeless")
                .setStatus(Status.FAILED)
                .setExtraBlock(CATEGORIES_BLOCK_NAME, singletonList(category));

        final CategoriesPlugin plugin = new CategoriesPlugin();
        final Tree<TestResult> tree = plugin.getData(
                createSingleLaunchResults(second, first, timeless)
        );

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .flatExtracting("children")
                .extracting("name")
                .containsExactly("timeless", "first", "second");
    }

    private TestResult createTestResult(String message, Status status) {
        return createTestResult(message, status, false);
    }

    private TestResult createTestResult(String message, Status status, boolean flaky) {
        return new TestResult().setStatus(status).setStatusDetails(
                new StatusDetails().setMessage(message).setFlaky(flaky)
        );
    }


}
