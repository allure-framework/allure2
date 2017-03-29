package io.qameta.allure.allure2;

import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.Main;
import io.qameta.allure.ReportInfo;
import io.qameta.allure.core.DefaultAttachmentsStorage;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.testdata.TestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.allure.AllureUtils.generateTestResultContainerName;
import static io.qameta.allure.AllureUtils.generateTestResultName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.matchers.nio.PathMatchers.contains;
import static ru.yandex.qatools.matchers.nio.PathMatchers.hasFilesCount;
import static ru.yandex.qatools.matchers.nio.PathMatchers.isDirectory;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure2ResultsReaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private AttachmentsStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new DefaultAttachmentsStorage();
    }

    @Test
    public void shouldCreateReportInfo() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, Collections.emptySet());

        Path results = folder.newFolder().toPath();
        TestData.unpackDummyResources("allure2data/", results);

        ReportInfo report = main.createReport(results);

        Assert.assertThat(report, notNullValue());
        Assert.assertThat(report.getResults(), hasSize(4));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadBeforesFromGroups() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        );

        assertThat(testResults, hasSize(1));
        TestCaseResult result = testResults.iterator().next();
        assertThat(result.getBeforeStages(), hasSize(2));
        assertThat(result.getBeforeStages(), hasItems(
                hasProperty("name", equalTo("mockAuthorization")),
                hasProperty("name", equalTo("loadTestConfiguration"))
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadAftersFromGroups() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        );

        assertThat(testResults, hasSize(1));
        TestCaseResult result = testResults.iterator().next();
        assertThat(result.getAfterStages(), hasSize(2));
        assertThat(result.getAfterStages(), hasItems(
                hasProperty("name", equalTo("cleanUpContext")),
                hasProperty("name", equalTo("unloadTestConfiguration"))
        ));
    }

    @Test
    public void shouldPickUpAttachmentsForTestCase() throws IOException {
        List<TestCaseResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/test-sample-attachment.txt", "test-sample-attachment.txt"
        );

        assertThat("Test case is not found", testResults, hasSize(1));
        TestCaseResult result = testResults.iterator().next();
        List<Step> steps = result.getTestStage().getSteps();
        assertThat("Test case should have one step", steps, hasSize(1));
        List<Attachment> attachments = result.getTestStage().getSteps().iterator().next().getAttachments();
        assertThat("Step should have an attachment", attachments, hasSize(1));
        assertThat("Attachment's name is unexpected",
                attachments.iterator().next().getName(), equalTo("String attachment in test"));
    }

    @Test
    public void shouldPickUpAttachmentsForAfters() throws IOException {
        List<TestCaseResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/after-sample-attachment.txt", "after-sample-attachment.txt"
        );

        assertThat("Test case is not found", testResults, hasSize(1));
        TestCaseResult result = testResults.iterator().next();
        List<StageResult> afters = result.getAfterStages();
        assertThat("Test case should have afters", afters, hasSize(2));

        List<Attachment> attachments = afters.stream()
                .map(StageResult::getAttachments)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertThat("Second after method should have an attachment",
                attachments, hasSize(1));
        assertThat("Attachment's name is unexpected",
                attachments.iterator().next().getName(), equalTo("String attachment in after"));
    }

    @Test
    public void shouldDoNotOverrideAttachmentsForGroups() throws IOException {
        List<TestCaseResult> testResults = process(
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/after-sample-attachment.txt", "after-sample-attachment.txt"
        );

        assertThat("Test cases is not found", testResults, hasSize(2));
        assertThat(testResults, allOf(
                iterableWithSize(2),
                everyItem(hasProperty("afterStages", allOf(
                        iterableWithSize(1),
                        everyItem(hasProperty("attachments", allOf(
                                iterableWithSize(1),
                                everyItem(hasProperty("name", equalTo("String attachment in after")))
                        )))
                )))
        ));
    }

    @Test
    public void shouldProcessEmptyStatus() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure2/no-status.json", generateTestResultName()
        );

        assertThat(testResults, hasSize(1));
        assertThat(testResults, hasItem(hasProperty("status", equalTo(Status.UNKNOWN))));
    }

    @Test
    public void shouldProcessNullStatus() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure2/null-status.json", generateTestResultName()
        );

        assertThat(testResults, hasSize(1));
        assertThat(testResults, hasItem(hasProperty("status", equalTo(Status.UNKNOWN))));
    }

    @Test
    public void shouldProcessInvalidStatus() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure2/invalid-status.json", generateTestResultName()
        );

        assertThat(testResults, hasSize(1));
        assertThat(testResults, hasItem(hasProperty("status", equalTo(Status.UNKNOWN))));
    }

    @Test
    public void shouldGenerateReport() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, null);
        Path output = folder.newFolder().toPath();
        Path results = folder.newFolder().toPath();
        TestData.unpackDummyResources("allure2data/", results);
        main.generate(output, results);
        assertThat(output, contains("index.html"));
        assertThat(output, contains("data"));
        Path data = output.resolve("data");
        assertThat(data, isDirectory());

        assertThat(data, contains("test-cases"));
        Path testCases = data.resolve("test-cases");
        assertThat(testCases, isDirectory());
        assertThat(testCases, hasFilesCount(4, "*.json"));

        assertThat(data, contains("category.json"));
        assertThat(data, contains("graph.json"));
        assertThat(data, contains("history.json"));
        assertThat(data, contains("timeline.json"));
        assertThat(data, contains("widgets.json"));
        assertThat(data, contains("xunit.json"));
        assertThat(data, contains("history.json"));
    }

    private List<TestCaseResult> process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        Allure2ResultsReader reader = new Allure2ResultsReader(storage);
        return reader.readResults(resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}