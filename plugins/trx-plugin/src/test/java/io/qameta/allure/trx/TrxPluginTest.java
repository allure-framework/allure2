/*
 *  Copyright 2016-2024 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.trx;

import io.qameta.allure.Issue;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.LabelName.TEST_CLASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class TrxPluginTest {

    private Configuration configuration;
    private ResultsVisitor visitor;
    private Path resultsDirectory;

    @BeforeEach
    void setUp(@TempDir final Path resultsDirectory) {
        configuration = mock(Configuration.class);
        when(configuration.requireContext(RandomUidContext.class)).thenReturn(new RandomUidContext());
        visitor = mock(ResultsVisitor.class);
        this.resultsDirectory = resultsDirectory;
    }

    @Test
    void shouldParseResults() throws Exception {
        process(
                "trxdata/sample.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
        verify(visitor, times(5)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getName, TestResult::getStatus, TestResult::getDescription)
                .containsExactlyInAnyOrder(
                        tuple("AddingSeveralNumbers_40", Status.PASSED, "Adding several numbers"),
                        tuple("AddingSeveralNumbers_60", Status.PASSED, "Adding several numbers"),
                        tuple("AddTwoNumbers", Status.PASSED, "Add two numbers"),
                        tuple("FailToAddTwoNumbers", Status.FAILED, "Fail to add two numbers"),
                        tuple("SkippedTest", Status.SKIPPED, "Should Skip this test")
                );

        assertThat(captor.getAllValues())
                .extracting(result -> result.findOneLabel(LabelName.RESULT_FORMAT))
                .extracting(Optional::get)
                .containsOnly(TrxPlugin.TRX_RESULTS_FORMAT);

    }

    @Issue("596")
    @Test
    void shouldParseErrorInfo() throws Exception {
        process(
                "trxdata/gh-596.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getStatusMessage, TestResult::getStatusTrace)
                .containsExactly(tuple("Some message", "Some trace"));
    }

    @Issue("749")
    @Test
    void shouldParseClassNameAsSuite() throws Exception {
        process(
                "trxdata/gh-749.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(result -> result.findOneLabel(LabelName.SUITE))
                .extracting(Optional::get)
                .containsOnly("TestClass");
    }

    @Test
    void shouldParseStdOutOnFail() throws Exception {
        process(
                "trxdata/sample.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
        verify(visitor, times(5)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .filteredOn(result -> result.getStatus() == Status.FAILED)
                .filteredOn(result -> result.getTestStage().getSteps().size() == 10)
                .filteredOn(result -> result.getTestStage().getSteps().get(1).getName().contains("Given I have entered 50 into the calculator"))
                .filteredOn(result -> result.getTestStage().getSteps().get(3).getName().contains("And I have entered -1 into the calculator"))
                .hasSize(1);
    }

    @Test
    void shouldParseTestResultChildren() throws Exception {
        process(
                "trxdata/testresultsWithChildren.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
        verify(visitor, times(7)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(7)
                .extracting(TestResult::getName, TestResult::getStatus)
                .containsExactlyInAnyOrder(
                        tuple("UnitTest_One", Status.PASSED),
                        tuple("UnitTest_Two", Status.PASSED),
                        tuple("UnitTest_Three", Status.FAILED),
                        tuple("UnitTest_Three (Child One)", Status.FAILED),
                        tuple("UnitTest_Three (Child Two)", Status.FAILED),
                        tuple("UnitTest_Three (Child Two) (GrandChild One)", Status.PASSED),
                        tuple("UnitTest_Three (Child Two) (GrandChild Two)", Status.FAILED)
                );

        List<Label> labels = new ArrayList<>();
        labels.add(new Label().setName(SUITE.value()).setValue("Test"));
        labels.add(new Label().setName(TEST_CLASS.value()).setValue("Test"));
        labels.add(new Label().setName(PACKAGE.value()).setValue("Test"));
        labels.add(new Label().setName(RESULT_FORMAT.value()).setValue("trx"));

        assertThat(captor.getAllValues())
                .filteredOn(result -> result.getName().contains("UnitTest_Three"))
                .extracting(TestResult::getLabels)
                .containsOnly(
                        labels
                );

    }

    @Test
    void cveEntityReadTest(@TempDir final Path tmp) throws IOException {
        final Path secretFile = tmp.resolve("secretfile.ini");
        Files.writeString(secretFile,
                "[owner]\n"
                + "name = John Doe\n"
                + "organization = Example Org.\n",
                StandardCharsets.UTF_8
        );

        Files.writeString(
                resultsDirectory.resolve("bad-test.trx"),
                "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE foo [\n"
                + "  <!ENTITY xxe SYSTEM \"" + secretFile.toUri() + "\">\n"
                + "]>\n"
                + "<TestRun id=\"37bd1bbc-784e-477a-8fe2-a116517ba93f\" name=\"@ip-10-0-12-95 2017-09-11 07:32:55\" xmlns=\"http://microsoft.com/schemas/VisualStudio/TeamTest/2010\">\n"
                + "    <Times creation=\"2017-09-11T07:32:55.5710479+00:00\" queuing=\"2017-09-11T07:32:55.5710493+00:00\" start=\"2017-09-11T07:31:47.7161493+00:00\" finish=\"2017-09-11T07:50:48.6048416+00:00\" />\n"
                + "    <TestSettings name=\"default\" id=\"fec1d7e5-efa1-43b5-b261-7507a1de835f\">\n"
                + "        <Deployment runDeploymentRoot=\"_ip-10-0-12-95 2017-09-11 07_32_55\" />\n"
                + "    </TestSettings>\n"
                + "    <Results>\n"
                + "        <UnitTestResult executionId=\"a0c122ad-b99a-4e42-b42c-9f03a42e789d\" testId=\"6efcec51-ecd8-464a-afdf-a8f254074a1a\" testName=\"MyCompany.TestSuite.IntegrationTests.Retrieve.RetrieveTestCases.Test_BookingIdInResponse_Succeeds\" computerName=\"ip-10-0-12-95\" duration=\"00:00:00.0010000\" startTime=\"2017-09-11T07:32:55.4659624+00:00\" endTime=\"2017-09-11T07:32:55.4659679+00:00\" testType=\"13cdc9d9-ddb5-4fa4-a97d-d965ccfc6d4b\" outcome=\"Failed\" testListId=\"8c84fa94-04c1-424b-9868-57a2d4851a1d\" relativeResultsDirectory=\"a0c122ad-b99a-4e42-b42c-9f03a42e789d\">\n"
                + "            <Output>\n"
                + "                <ErrorInfo>\n"
                + "                    <Message>&xxe;</Message>\n"
                + "                    <StackTrace>&xxe;</StackTrace>\n"
                + "                </ErrorInfo>\n"
                + "            </Output>\n"
                + "        </UnitTestResult>\n"
                + "    </Results>\n"
                + "    <TestDefinitions>\n"
                + "        <UnitTest name=\"MyCompany.TestSuite.IntegrationTests.Retrieve.RetrieveTestCases.Test_BookingIdInResponse_Succeeds\" storage=\"/var/lib/jenkins/workspace/Product/code/test/MyCompany.testsuite/bin/release/netcoreapp2.0/MyCompany.testsuite.dll\" id=\"6efcec51-ecd8-464a-afdf-a8f254074a1a\">\n"
                + "            <Execution id=\"a0c122ad-b99a-4e42-b42c-9f03a42e789d\" />\n"
                + "            <TestMethod codeBase=\"/var/lib/jenkins/workspace/Product/code/test/MyCompany.TestSuite/bin/Release/netcoreapp2.0/MyCompany.TestSuite.dll\" executorUriOfAdapter=\"executor://xunit/VsTestRunner2\" className=\"MyCompany.TestSuite.IntegrationTests.Retrieve.RetrieveTestCases\" name=\"MyCompany.TestSuite.IntegrationTests.Retrieve.RetrieveTestCases.Test_BookingIdInResponse_Succeeds\" />\n"
                + "        </UnitTest>\n"
                + "    </TestDefinitions>\n"
                + "    <TestEntries>\n"
                + "        <TestEntry testId=\"6efcec51-ecd8-464a-afdf-a8f254074a1a\" executionId=\"a0c122ad-b99a-4e42-b42c-9f03a42e789d\" testListId=\"8c84fa94-04c1-424b-9868-57a2d4851a1d\" />\n"
                + "    </TestEntries>\n"
                + "    <TestLists>\n"
                + "        <TestList name=\"Results Not in a List\" id=\"8c84fa94-04c1-424b-9868-57a2d4851a1d\" />\n"
                + "        <TestList name=\"All Loaded Results\" id=\"19431567-8539-422a-85d7-44ee4e166bda\" />\n"
                + "    </TestLists>\n"
                + "</TestRun>",
                StandardCharsets.UTF_8
        );

        TrxPlugin reader = new TrxPlugin();

        reader.readResults(configuration, visitor, resultsDirectory);

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
        verify(visitor, times(1)).visitTestResult(captor.capture());

        final TestResult testResult = captor.getValue();
        assertThat(testResult.getStatusMessage()).doesNotContain("John Doe");
        assertThat(testResult.getStatusTrace()).doesNotContain("Example Org");
    }

    private void process(String... strings) throws IOException {
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        TrxPlugin reader = new TrxPlugin();
        reader.readResults(configuration, visitor, resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(Objects.requireNonNull(is), dir.resolve(fileName));
        }
    }
}
