package org.allurefw.report.junit;

import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.core.DefaultAttachmentsStorage;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.junit.JunitResultsReader;
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
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitTestResultsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private AttachmentsStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new DefaultAttachmentsStorage();
    }

    @Test
    public void shouldReadJunitResults() throws Exception {
        List<TestCaseResult> testCases = process(
                "junitdata/TEST-org.allurefw.report.junit.JunitTestResultsTest.xml",
                "TEST-org.allurefw.report.junit.JunitTestResultsTest.xml"
        );

        assertThat(testCases, hasSize(5));

        List<TestCaseResult> failed = filterByStatus(testCases, Status.FAILED);
        List<TestCaseResult> skipped = filterByStatus(testCases, Status.SKIPPED);
        List<TestCaseResult> passed = filterByStatus(testCases, Status.PASSED);

        assertThat("Should parse failed status", failed, hasSize(1));
        assertThat("Should parse skipped status", skipped, hasSize(1));
        assertThat("Should parse passed status", passed, hasSize(3));
    }

    @Test
    public void shouldAddLogAsAttachment() throws Exception {
        List<TestCaseResult> testCases = process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml",
                "junitdata/test.SampleTest.txt", "test.SampleTest.txt"
        );

        assertThat(storage.getAttachments().entrySet(), hasSize(1));
        Attachment attachment = storage.getAttachments().values().iterator().next();

        assertThat(attachment.getName(), is("System out"));

        TestCaseResult result = testCases.iterator().next();
        assertThat(result.getTestStage().getAttachments(), hasSize(1));

        Attachment resultAttachment = result.getTestStage().getAttachments().iterator().next();
        assertThat(resultAttachment, is(attachment));
    }

    @Test
    @Ignore("Add support of retries")
    public void shouldProcessTestsWithRetry() throws Exception {
        List<TestCaseResult> testCases = process(
                "junitdata/TEST-test.RetryTest.xml", "TEST-test.SampleTest.xml"
        );

        assertThat(testCases, hasSize(3));
    }

    @Test
    public void shouldSkipInvalidXml() throws Exception {
        List<TestCaseResult> testCases = process(
                "junitdata/invalid.xml", "sample-testsuite.xml"
        );

        assertThat(testCases, hasSize(0));
    }

    private List<TestCaseResult> process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        JunitResultsReader reader = new JunitResultsReader(storage);
        return reader.readResults(resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }

    private List<TestCaseResult> filterByStatus(List<TestCaseResult> testCases, Status status) {
        return testCases.stream()
                .filter(item -> status.equals(item.getStatus()))
                .collect(Collectors.toList());
    }
}