package org.allurefw.report.allure1;

import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.core.DefaultAttachmentsStorage;
import org.allurefw.report.entity.Label;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure1TestResultsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private AttachmentsStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new DefaultAttachmentsStorage();
    }

    @Test
    public void shouldReadTestSuiteXml() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName()
        );
        assertThat(testResults, hasSize(4));
    }

    @Test
    public void shouldReadTestSuiteJson() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        );
        assertThat(testResults, hasSize(1));
    }

    @Test
    public void shouldReadAttachments() throws Exception {
        process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName(),
                "allure1/sample-attachment.txt", "sample-attachment.txt"
        );

        assertThat(storage.getAttachments().entrySet(), hasSize(1));
    }

    @Test
    @Ignore("Not implemented yet")
    public void shouldReadEnvironmentProperties() throws Exception {
        process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.properties", "environment.properties"
        );
    }

    @Test
    @Ignore("Not implemented yet")
    public void shouldReadEnvironmentXml() throws Exception {
        process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.xml", "environment.xml"
        );
    }

    @Test
    public void shouldNotFailIfNoResultsDirectory() throws Exception {
        List<TestCaseResult> testResults = process();
        assertThat(testResults, empty());
    }

    @Test
    public void shouldGetSuiteTitleIfExists() throws Exception {
        List<TestCaseResult> testCases = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        );
        assertThat(testCases, hasSize(1));
        TestCaseResult result = testCases.iterator().next();
        Optional<String> suiteName = result.findOne(LabelName.SUITE);
        assertThat(suiteName, isPresent());
        assertThat(suiteName, hasValue("Passing test"));
    }

    @Test
    public void shouldNotFailIfSuiteTitleNotExists() throws Exception {
        List<TestCaseResult> testCases = process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName()
        );
        assertThat(testCases, hasSize(1));
        TestCaseResult result = testCases.iterator().next();
        Optional<String> suiteName = result.findOne(LabelName.SUITE);
        assertThat(suiteName, isPresent());
        assertThat(suiteName, hasValue("my.company.AlwaysPassingTest"));
    }

    @Test
    public void shouldCopyLabelsFromSuite() throws Exception {
        List<TestCaseResult> testCases = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        );
        assertThat(testCases, hasSize(1));
        TestCaseResult result = testCases.iterator().next();
        List<String> stories = result.getLabels().stream()
                .filter(label -> "story".equals(label.getName()))
                .map(Label::getValue)
                .collect(Collectors.toList());
        assertThat(stories, hasSize(2));
        assertThat(stories, hasItems("SuccessStory", "OtherStory"));
    }

    private List<TestCaseResult> process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        Allure1ResultsReader reader = new Allure1ResultsReader(storage);
        return reader.readResults(resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}