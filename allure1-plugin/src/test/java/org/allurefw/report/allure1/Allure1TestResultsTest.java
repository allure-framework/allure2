package org.allurefw.report.allure1;

import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;
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
import java.util.Map;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure1TestResultsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldReadTestSuiteXml() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName()
        );
        assertThat(testResults.getTestCases(), hasSize(4));
    }

    @Test
    public void shouldReadTestSuiteJson() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        );
        assertThat(testResults.getTestCases(), hasSize(1));
    }

    @Test
    public void shouldReadAttachments() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName(),
                "allure1/sample-attachment.txt", "sample-attachment.txt"
        );

        Map<Path, Attachment> attachments = testResults.getAttachments();
        assertThat(attachments.entrySet(), hasSize(1));
    }

    @Test
    public void shouldReadEnvironmentProperties() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.properties", "environment.properties"
        );
        testResults.getTestCases().forEach(result -> {
            assertThat(result.getEnvironment(), notNullValue());
            assertThat(result.getEnvironment(), hasSize(5));
        });
    }

    @Test
    public void shouldReadEnvironmentXml() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.xml", "environment.xml"
        );
        testResults.getTestCases().forEach(result -> {
            assertThat(result.getEnvironment(), notNullValue());
            assertThat(result.getEnvironment(), hasSize(2));
        });
    }

    @Test
    public void shouldAddSuiteGroup() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName()
        );
        List<TestGroup> testGroups = testResults.getTestGroups();
        assertThat(testGroups, hasSize(1));
        TestGroup testGroup = testGroups.iterator().next();
        assertThat(testGroup.getType(), is("suite"));

        TestCaseResult result = testResults.getTestCases().iterator().next();
        Optional<String> suiteName = result.findOne(LabelName.SUITE);
        assertThat(suiteName, isPresent());
        assertThat(suiteName, hasValue(testGroup.getName()));
    }

    @Test
    public void shouldNotFailIfNoResultsDirectory() throws Exception {
        Allure1TestsResults testResults = new Allure1TestsResults(folder.newFile().toPath());
        assertThat(testResults.getTestCases().iterator().hasNext(), is(false));
        assertThat(testResults.getTestGroups(), hasSize(0));
    }

    @Test
    public void shouldGetSuiteTitleIfExists() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        );
        List<TestGroup> testGroups = testResults.getTestGroups();
        assertThat(testGroups, hasSize(1));
        TestGroup testGroup = testGroups.iterator().next();
        assertThat(testGroup.getName(), is("Passing test"));

        List<TestCaseResult> testCases = testResults.getTestCases();
        assertThat(testCases, hasSize(1));
        TestCaseResult result = testCases.iterator().next();
        Optional<String> suiteName = result.findOne(LabelName.SUITE);
        assertThat(suiteName, isPresent());
        assertThat(suiteName, hasValue("Passing test"));
    }

    @Test
    public void shouldNotFailIfSuiteTitleNotExists() throws Exception {
        Allure1TestsResults testResults = process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName()
        );
        List<TestGroup> testGroups = testResults.getTestGroups();
        assertThat(testGroups, hasSize(1));
        TestGroup testGroup = testGroups.iterator().next();
        assertThat(testGroup.getName(), is("my.company.AlwaysPassingTest"));

        List<TestCaseResult> testCases = testResults.getTestCases();
        assertThat(testCases, hasSize(1));
        TestCaseResult result = testCases.iterator().next();
        Optional<String> suiteName = result.findOne(LabelName.SUITE);
        assertThat(suiteName, isPresent());
        assertThat(suiteName, hasValue("my.company.AlwaysPassingTest"));
    }

    public Allure1TestsResults process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        return new Allure1TestsResults(resultsDirectory);
    }

    public void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}