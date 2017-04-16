package io.qameta.allure.allure2;

import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.util.TestDataProcessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Set;

import static io.qameta.allure.AllureUtils.generateTestResultContainerName;
import static io.qameta.allure.AllureUtils.generateTestResultName;
import static io.qameta.allure.entity.Status.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

public class Allure2PluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private TestDataProcessor helper;

    @Before
    public void prepare() throws IOException {
        helper = new TestDataProcessor(folder.newFolder().toPath(), new Allure2Plugin());
    }

    @Test
    public void shouldReadBeforesFromGroups() throws IOException {
        Set<TestResult> testResults = helper.processResources(
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
        Set<TestResult> testResults = helper.processResources(
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
    public void shouldPickUpAttachmentsForTestCase() throws IOException {
        Set<TestResult> testResults = helper.processResources(
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
        Set<TestResult> testResults = helper.processResources(
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
        Set<TestResult> testResults = helper.processResources(
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
        Set<TestResult> testResults = helper.processResources(
                "allure2/no-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    public void shouldProcessNullStatus() throws Exception {
        Set<TestResult> testResults = helper.processResources(
                "allure2/null-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    public void shouldProcessInvalidStatus() throws Exception {
        Set<TestResult> testResults = helper.processResources(
                "allure2/invalid-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    public void shouldProcessNullStageTime() throws Exception {
        Set<TestResult> testResults = helper.processResources(
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/null-before-group.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1);
    }
}