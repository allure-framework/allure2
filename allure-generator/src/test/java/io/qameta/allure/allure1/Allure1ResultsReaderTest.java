package io.qameta.allure.allure1;

import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.core.DefaultAttachmentsStorage;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestCaseResult;
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
import java.util.stream.Stream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.UNKNOWN;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure1ResultsReaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private AttachmentsStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new DefaultAttachmentsStorage();
    }

    @Test
    public void shouldProcessEmptyOrNullStatus() throws Exception {
        List<TestCaseResult> testResults = process(
                "allure1/empty-status-testsuite.xml", generateTestSuiteXmlName()
        );
        assertThat(testResults, hasSize(4));
        assertThat(testResults, hasItems(
                allOf(hasProperty("name", equalTo("testOne")), hasProperty("status", equalTo(UNKNOWN))),
                allOf(hasProperty("name", equalTo("testTwo")), hasProperty("status", equalTo(PASSED))),
                allOf(hasProperty("name", equalTo("testThree")), hasProperty("status", equalTo(FAILED))),
                allOf(hasProperty("name", equalTo("testFour")), hasProperty("status", equalTo(UNKNOWN)))
        ));
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
        List<TestCaseResult> results = process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName(),
                "allure1/sample-attachment.txt", "sample-attachment.txt"
        );

        assertThat(storage.getAttachments().entrySet(), hasSize(1));
        List<Attachment> attachments = results.stream()
                .flatMap(this::extractAttachments)
                .collect(Collectors.toList());
        assertThat(attachments, hasSize(1));
        Attachment a = storage.getAttachments().values().iterator().next();
        Attachment b = attachments.iterator().next();
        assertThat(a.getSource(), is(b.getSource()));
    }

    private Stream<Attachment> extractAttachments(TestCaseResult testCaseResult) {
        Stream<StageResult> before = testCaseResult.getBeforeStages().stream();
        Stream<StageResult> test = Stream.of(testCaseResult.getTestStage());
        Stream<StageResult> after = testCaseResult.getAfterStages().stream();
        return Stream.concat(before, Stream.concat(test, after))
                .flatMap(this::extractAttachments);
    }

    private Stream<Attachment> extractAttachments(StageResult stageResult) {
        Stream<Attachment> fromSteps = stageResult.getSteps().stream().flatMap(this::extractAttachments);
        Stream<Attachment> fromAttachments = stageResult.getAttachments().stream();
        return Stream.concat(fromSteps, fromAttachments);
    }

    private Stream<Attachment> extractAttachments(Step step) {
        Stream<Attachment> fromSteps = step.getSteps().stream().flatMap(this::extractAttachments);
        Stream<Attachment> fromAttachments = step.getAttachments().stream();
        return Stream.concat(fromSteps, fromAttachments);
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