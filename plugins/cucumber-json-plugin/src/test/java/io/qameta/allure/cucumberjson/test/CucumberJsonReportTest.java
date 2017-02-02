package io.qameta.allure.cucumberjson.test;

import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.core.DefaultAttachmentsStorage;
import io.qameta.allure.cucumberjson.CucumberJsonResultsReader;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestCaseResult;
import org.junit.Before;
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

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class CucumberJsonReportTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private AttachmentsStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new DefaultAttachmentsStorage();
    }

    @Test
    public void readSingleJsonFile() throws Exception {
        List<TestCaseResult> testCases = process("simple.json", "simple.json");
        List<Status> statuses = Arrays.asList(Status.BROKEN, Status.FAILED, Status.PASSED, Status.CANCELED);

        assertThat(testCases, hasSize(4));

        statuses.forEach(status -> {
                    List<TestCaseResult> found = testCases.stream()
                            .filter(item -> status.equals(item.getStatus()))
                            .collect(Collectors.toList());
                    assertThat(format("Should parse %s status", status.name()), found, hasSize(1));
                }
        );
    }

    @Test
    public void readEmptyJsonFile() throws Exception {
        List<TestCaseResult> results = process("empty.json", "empty.json");

        assertThat(results, hasSize(0));
    }

    @Test
    public void readInvalidJsonFile() throws Exception {
        List<TestCaseResult> results = process("invalid.json", "invalid.json");

        assertThat(results, hasSize(0));
    }

    @Test
    public void readJsonWithAttachment() throws Exception {
        List<TestCaseResult> results = process("complex.json", "complex.json");

        final List<Step> steps = results.get(0).getTestStage().getSteps();
        assertThat("Steps have not been parsed", steps, hasSize(4));

        List<Attachment> attachments = steps.get(0).getAttachments();
        assertThat("Attachments have not been processed", attachments, hasSize(1));
        assertThat("Unexpected attachment", attachments.get(0).getName(),
                equalTo("embedding_-115657502.image"));
    }

    @Test
    public void readSeveralJsonFiles() throws Exception {
        List<TestCaseResult> testCases = process("simple.json", "simple.json",
                "complex.json", "complex.json"
        );

        assertThat("Unexpected quantity of test cases from several parsed features",
                testCases, hasSize(8));
    }

    private List<TestCaseResult> process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        CucumberJsonResultsReader reader = new CucumberJsonResultsReader(storage);
        return reader.readResults(resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}
