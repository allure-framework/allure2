package io.qameta.allure.allure1;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.util.TestDataProcessor;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.UNKNOWN;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.assertj.core.api.Assertions.assertThat;

public class Allure1PluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private TestDataProcessor dataProcessor;

    @Before
    public void prepare() throws IOException {
        dataProcessor = new TestDataProcessor(folder.newFolder().toPath(), new Allure1Plugin());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProcessEmptyOrNullStatus() throws Exception {
        Set<TestResult> testResults = dataProcessor.processResources(
                "allure1/empty-status-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(4)
                .extracting("name", "status")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("testOne", UNKNOWN),
                        Tuple.tuple("testTwo", PASSED),
                        Tuple.tuple("testThree", FAILED),
                        Tuple.tuple("testFour", UNKNOWN)
                );
    }

    @Test
    public void shouldReadTestSuiteXml() throws Exception {
        Set<TestResult> testResults = dataProcessor.processResources(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(4);
    }

    @Test
    public void shouldReadTestSuiteJson() throws Exception {
        Set<TestResult> testResults = dataProcessor.processResources(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        ).getResults();
        assertThat(testResults)
                .hasSize(1);
    }

    @Test
    public void shouldReadAttachments() throws Exception {
        final LaunchResults launchResults = dataProcessor.processResources(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName(),
                "allure1/sample-attachment.txt", "sample-attachment.txt"
        );
        final Map<Path, Attachment> attachmentMap = launchResults.getAttachments();
        final Set<TestResult> results = launchResults.getResults();

        assertThat(attachmentMap)
                .hasSize(1);

        final Attachment storedAttachment = attachmentMap.values().iterator().next();

        List<Attachment> attachments = results.stream()
                .flatMap(this::extractAttachments)
                .collect(Collectors.toList());

        assertThat(attachments)
                .hasSize(1)
                .extracting(Attachment::getSource)
                .containsExactly(storedAttachment.getSource());
    }

    private Stream<Attachment> extractAttachments(TestResult testCaseResult) {
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
        dataProcessor.processResources(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.properties", "environment.properties"
        );
    }

    @Test
    @Ignore("Not implemented yet")
    public void shouldReadEnvironmentXml() throws Exception {
        dataProcessor.processResources(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName(),
                "allure1/environment.xml", "environment.xml"
        );
    }

    @Test
    public void shouldNotFailIfNoResultsDirectory() throws Exception {
        Set<TestResult> testResults = dataProcessor.processResources().getResults();
        assertThat(testResults)
                .isEmpty();
    }

    @Test
    public void shouldGetSuiteTitleIfExists() throws Exception {
        Set<TestResult> testCases = dataProcessor.processResources(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        ).getResults();
        assertThat(testCases)
                .hasSize(1)
                .extracting(testResult -> testResult.findOne(LabelName.SUITE))
                .extracting(Optional::get)
                .containsExactly("Passing test");
    }

    @Test
    public void shouldNotFailIfSuiteTitleNotExists() throws Exception {
        Set<TestResult> testCases = dataProcessor.processResources(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(testCases)
                .hasSize(1)
                .extracting(testResult -> testResult.findOne(LabelName.SUITE))
                .extracting(Optional::get)
                .containsExactly("my.company.AlwaysPassingTest");
    }

    @Test
    public void shouldCopyLabelsFromSuite() throws Exception {
        Set<TestResult> testCases = dataProcessor.processResources(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        ).getResults();
        assertThat(testCases)
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .filteredOn(label -> LabelName.STORY.value().equals(label.getName()))
                .hasSize(2)
                .extracting(Label::getValue)
                .containsExactlyInAnyOrder("SuccessStory", "OtherStory");
    }

    @Test
    public void shouldSetFlakyFromLabel() throws Exception {
        Set<TestResult> testCases = dataProcessor.processResources(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testCases)
                .hasSize(1)
                .extracting(TestResult::getStatusDetails)
                .extracting(StatusDetails::isFlaky)
                .containsExactly(true);
    }

    @Test
    public void shouldUseTestClassLabelForPackage() throws Exception {
        Set<TestResult> testResults = dataProcessor.processResources(
                "allure1/packages-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(1)
                .extracting(result -> result.findOne(LabelName.PACKAGE))
                .extracting(Optional::get)
                .containsExactly("my.company.package.subpackage.MyClass");
    }

    @Test
    public void shouldUseTestClassLabelForFullName() throws Exception {
        Set<TestResult> testResults = dataProcessor.processResources(
                "allure1/packages-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getFullName)
                .containsExactly("my.company.package.subpackage.MyClass.testThree");
    }
}