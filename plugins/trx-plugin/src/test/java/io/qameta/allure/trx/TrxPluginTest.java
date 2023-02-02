/*
 *  Copyright 2016-2023 Qameta Software OÜ
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

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(4)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(4)
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

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
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

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
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

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(4)).visitTestResult(captor.capture());

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

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
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
