package org.allurefw.report.allure2;

import com.google.common.collect.Iterables;
import com.google.common.reflect.ClassPath;
import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.Main;
import org.allurefw.report.ReportInfo;
import org.allurefw.report.core.DefaultAttachmentsStorage;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.StageResult;
import org.allurefw.report.entity.Step;
import org.allurefw.report.entity.TestCaseResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.qameta.allure.AllureUtils.generateTestCaseJsonFileName;
import static io.qameta.allure.AllureUtils.generateTestGroupJsonFileName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
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

        ReportInfo report = main.createReport(getDataFolder("allure2data/"));

        Assert.assertThat(report, notNullValue());
        Assert.assertThat(report.getResults(), hasSize(4));
    }

    @Test
    public void shouldReadBeforesFromGroups() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure2/simple-testcase.json", generateTestCaseJsonFileName(),
                "allure2/first-testgroup.json", generateTestGroupJsonFileName(),
                "allure2/second-testgroup.json", generateTestGroupJsonFileName()
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
    public void shouldReadAftersFromGroups() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure2/simple-testcase.json", generateTestCaseJsonFileName(),
                "allure2/first-testgroup.json", generateTestGroupJsonFileName(),
                "allure2/second-testgroup.json", generateTestGroupJsonFileName()
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
                "allure2/simple-testcase.json", generateTestCaseJsonFileName(),
                "allure2/first-testgroup.json", generateTestGroupJsonFileName(),
                "allure2/second-testgroup.json", generateTestGroupJsonFileName(),
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
                "allure2/simple-testcase.json", generateTestCaseJsonFileName(),
                "allure2/first-testgroup.json", generateTestGroupJsonFileName(),
                "allure2/second-testgroup.json", generateTestGroupJsonFileName(),
                "allure2/after-sample-attachment.txt", "after-sample-attachment.txt"
        );

        assertThat("Test case is not found", testResults, hasSize(1));
        TestCaseResult result = testResults.iterator().next();
        List<StageResult> afters = result.getAfterStages();
        assertThat("Test case should have afters", afters, hasSize(2));
        List<Attachment> attachments = Iterables.getLast(afters).getAttachments();
        assertThat("Second after method should have an attachment",
                attachments, hasSize(1));
        assertThat("Attachment's name is unexpected",
                attachments.iterator().next().getName(), equalTo("String attachment in after"));
    }

    @Test
    public void shouldDoNotOverrideAttachmentsForGroups() throws IOException {
        List<TestCaseResult> testResults = process(
                "allure2/other-testcase.json", generateTestCaseJsonFileName(),
                "allure2/other-testcase.json", generateTestCaseJsonFileName(),
                "allure2/second-testgroup.json", generateTestGroupJsonFileName(),
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
    public void shouldGenerateReport() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, null);
        Path output = folder.newFolder().toPath();
        main.generate(output, getDataFolder("allure2data/"));
        assertThat(output, contains("index.html"));
        assertThat(output, contains("data"));
        Path data = output.resolve("data");
        assertThat(data, isDirectory());

        assertThat(data, contains("test-cases"));
        Path testCases = data.resolve("test-cases");
        assertThat(testCases, isDirectory());
        assertThat(testCases, hasFilesCount(4, "*.json"));

        assertThat(data, contains("defects.json"));
        assertThat(data, contains("graph.json"));
        assertThat(data, contains("history.json"));
        assertThat(data, contains("timeline.json"));
        assertThat(data, contains("widgets.json"));
        assertThat(data, contains("xunit.json"));
        assertThat(data, contains("history.json"));
    }


    private Path getDataFolder(String prefix) throws IOException {
        ClassPath classPath = ClassPath.from(getClass().getClassLoader());
        Map<String, URL> files = classPath.getResources().stream()
                .filter(info -> info.getResourceName().startsWith(prefix))
                .collect(Collectors.toMap(
                        info -> info.getResourceName().substring(prefix.length()),
                        ClassPath.ResourceInfo::url)
                );
        Path dir = folder.newFolder().toPath();
        files.forEach((name, url) -> {
            Path file = dir.resolve(name);
            try (InputStream is = url.openStream()) {
                Files.copy(is, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return dir;
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