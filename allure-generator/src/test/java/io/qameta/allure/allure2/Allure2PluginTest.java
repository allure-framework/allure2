package io.qameta.allure.allure2;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static io.qameta.allure.AllureUtils.generateTestResultContainerName;
import static io.qameta.allure.AllureUtils.generateTestResultName;
import static io.qameta.allure.entity.Status.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class Allure2PluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldReadBeforesFromGroups() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .flatExtracting(TestResult::getBeforeStages)
                .hasSize(2)
                .extracting(StageResult::getName)
                .containsExactlyInAnyOrder("mockAuthorization", "loadTestConfiguration");
    }

    @Test
    public void shouldReadAftersFromGroups() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .flatExtracting(TestResult::getAfterStages)
                .hasSize(2)
                .extracting(StageResult::getName)
                .containsExactlyInAnyOrder("unloadTestConfiguration", "cleanUpContext");
    }

    @Test
    public void shouldExcludeDuplicatedParams() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/duplicated-params.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .flatExtracting(TestResult::getParameters)
                .extracting(Parameter::getName, Parameter::getValue)
                .containsExactlyInAnyOrder(
                        tuple("name", "value"),
                        tuple("name2", "value"),
                        tuple("name", "value2"),
                        tuple("name2", "value2")
                );
    }

    @Test
    public void shouldPickUpAttachmentsForTestCase() throws IOException {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/test-sample-attachment.txt", "test-sample-attachment.txt"
        ).getResults();

        assertThat(testResults)
                .describedAs("Test case is not found")
                .hasSize(1)
                .extracting(TestResult::getTestStage)
                .flatExtracting(StageResult::getSteps)
                .describedAs("Test case should have one step")
                .hasSize(1)
                .flatExtracting(Step::getAttachments)
                .describedAs("Step should have an attachment")
                .hasSize(1)
                .extracting(Attachment::getName)
                .containsExactly("String attachment in test");
    }

    @Test
    public void shouldPickUpAttachmentsForAfters() throws IOException {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/after-sample-attachment.txt", "after-sample-attachment.txt"
        ).getResults();

        assertThat(testResults)
                .describedAs("Test case is not found")
                .hasSize(1)
                .flatExtracting(TestResult::getAfterStages)
                .describedAs("Test case should have afters")
                .hasSize(2)
                .flatExtracting(StageResult::getAttachments)
                .describedAs("Second after method should have an attachment")
                .hasSize(1)
                .extracting(Attachment::getName)
                .describedAs("Attachment's name is unexpected")
                .containsExactly("String attachment in after");
    }

    @Test
    public void shouldDoNotOverrideAttachmentsForGroups() throws IOException {
        Set<TestResult> testResults = process(
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/after-sample-attachment.txt", "after-sample-attachment.txt"
        ).getResults();

        assertThat(testResults)
                .describedAs("Test cases is not found")
                .hasSize(2);

        testResults.forEach(testResult -> assertThat(testResult.getAfterStages())
                .hasSize(1)
                .flatExtracting(StageResult::getAttachments)
                .hasSize(1)
                .extracting(Attachment::getName)
                .containsExactly("String attachment in after"));

    }

    @Test
    public void shouldProcessEmptyStatus() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/no-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    public void shouldProcessNullStatus() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/null-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    public void shouldProcessInvalidStatus() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/invalid-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    public void shouldProcessNullStageTime() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/null-before-group.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1);
    }

    @Test
    public void shouldAddTestResultFormatLabel() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .extracting(result -> result.findOneLabel(LabelName.RESULT_FORMAT))
                .extracting(Optional::get)
                .containsOnly(Allure2Plugin.ALLURE2_RESULTS_FORMAT);
    }

    private LaunchResults process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        Allure2Plugin reader = new Allure2Plugin();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
        reader.readResults(configuration, resultsVisitor, resultsDirectory);
        return resultsVisitor.getLaunchResults();
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}