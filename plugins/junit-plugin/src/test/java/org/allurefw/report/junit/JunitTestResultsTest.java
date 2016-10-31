package org.allurefw.report.junit;

import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.core.DefaultAttachmentsStorage;
import org.allurefw.report.entity.Attachment;
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
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml"
        );

        assertThat(testCases, hasSize(1));
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
        assertThat(result.getAttachments(), hasSize(1));

        Attachment resultAttachment = result.getAttachments().iterator().next();
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
}