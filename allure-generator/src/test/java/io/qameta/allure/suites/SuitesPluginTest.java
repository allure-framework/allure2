package io.qameta.allure.suites;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Issue;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestLabel;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.Node;
import io.qameta.allure.tree.TestResultTree;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.qameta.allure.suites.SuitesPlugin.CSV_FILE_NAME;
import static io.qameta.allure.suites.SuitesPlugin.JSON_FILE_NAME;
import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class SuitesPluginTest {

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
    public void shouldCreateTree() throws Exception {
        final TestResultTree tree = SuitesPlugin.getData(getSimpleLaunchResults());

        assertThat(tree.getChildren())
                .hasSize(2)
                .extracting(Node::getName)
                .containsExactlyInAnyOrder("s1", "s2");
    }

    @Issue("587")
    @Issue("572")
    @Test
    public void shouldSortByStartTimeAsc() throws Exception {
        final TestResult first = new TestResult()
                .setName("first")
                .setStart(10L);
        final TestResult second = new TestResult()
                .setName("second")
                .setStart(12L);
        final TestResult timeless = new TestResult()
                .setName("timeless");

        final TestResultTree tree = SuitesPlugin.getData(
                createSingleLaunchResults(second, first, timeless)
        );

        assertThat(tree.getChildren())
                .extracting(Node::getName)
                .containsExactly("timeless", "first", "second");
    }

    @Test
    public void shouldCreateCsvFile() throws IOException {

        final SuitesPlugin plugin = new SuitesPlugin();

        plugin.aggregate(configuration, getSimpleLaunchResults(), reportPath);

        assertThat(reportPath.resolve("data").resolve(JSON_FILE_NAME))
                .exists();

        assertThat(reportPath.resolve("data").resolve(CSV_FILE_NAME))
                .exists();
    }

    private List<LaunchResults> getSimpleLaunchResults() {
        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(singleton(new TestLabel().setName("suite").setValue("s1")));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(singleton(new TestLabel().setName("suite").setValue("s1")));
        final TestResult third = new TestResult()
                .setName("third")
                .setLabels(singleton(new TestLabel().setName("suite").setValue("s2")));
        return createSingleLaunchResults(second, first, third);
    }
}