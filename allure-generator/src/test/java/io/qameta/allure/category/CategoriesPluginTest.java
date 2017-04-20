package io.qameta.allure.category;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * eroshenkoam
 * 20.04.17
 */
public class CategoriesPluginTest {

    private static final String CATEGORY_NAME = "Category";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path reportPath;

    @Before
    public void setUp() throws IOException {
        reportPath = Paths.get(folder.newFolder("report").getAbsolutePath());
    }

    @Test
    public void shouldWork() throws IOException {
        Configuration configuration = new ConfigurationBuilder().useDefault().build();

        Category category = new Category()
                .withName(CATEGORY_NAME)
                .withMessageRegex(".*")
                .withMatchedStatuses(Status.BROKEN);

        Map<String, Object> meta = new HashMap<>();
        meta.put(CategoriesPlugin.CATEGORIES, Collections.singletonList(category));

        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                meta, createTestResult("asd\n", Status.BROKEN)
        );

        CategoriesPlugin plugin = new CategoriesPlugin();

        plugin.aggregate(configuration, launchResultsList, reportPath);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();
        List<Category> categories = results.toArray(new TestResult[]{})[0]
                .getExtraBlock(CategoriesPlugin.CATEGORIES);

        assertThat(categories).as("test categories")
                .extracting(Category::getName)
                .containsExactly(CATEGORY_NAME);

        assertThat(reportPath.resolve("data").resolve("categories.json"))
                .exists();

    }


    private TestResult createTestResult(String message, Status status) {
        return new TestResult().withStatus(status).withStatusDetails(new StatusDetails().withMessage(message));
    }


}
