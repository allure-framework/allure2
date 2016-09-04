package org.allurefw.report.junit;

import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;
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

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitTestResultsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldReadJunitResults() throws Exception {
        JunitTestsResults testResults = process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml"
        );

        assertThat(testResults.getTestCases(), hasSize(1));
    }

    @Test
    public void shouldReadSuiteGroup() throws Exception {
        JunitTestsResults testResults = process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml"
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
    public void shouldAddLogAsAttachment() throws Exception {
        JunitTestsResults testResults = process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml",
                "junitdata/test.SampleTest.txt", "test.SampleTest.txt"
        );

        assertThat(testResults.getAttachments().entrySet(), hasSize(1));
        Attachment attachment = testResults.getAttachments().values().iterator().next();

        assertThat(attachment.getName(), is("test.SampleTest"));

        TestCaseResult result = testResults.getTestCases().iterator().next();
        assertThat(result.getAttachments(), hasSize(1));

        Attachment resultAttachment = result.getAttachments().iterator().next();
        assertThat(resultAttachment, is(attachment));
    }

    @Test
    @Ignore("Add support of retries")
    public void shouldProcessTestsWithRetry() throws Exception {
        JunitTestsResults testResults = process(
                "junitdata/TEST-test.RetryTest.xml", "TEST-test.SampleTest.xml"
        );

        assertThat(testResults.getTestCases(), hasSize(3));
    }

    public JunitTestsResults process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        return new JunitTestsResults(resultsDirectory);
    }

    public void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}