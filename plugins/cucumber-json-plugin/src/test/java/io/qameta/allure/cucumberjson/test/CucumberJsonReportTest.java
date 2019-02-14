/*
 *  Copyright 2019 Qameta Software OÜ
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
package io.qameta.allure.cucumberjson.test;

import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.core.DefaultAttachmentsStorage;
import io.qameta.allure.cucumberjson.CucumberJsonResultsReader;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.StageResult;
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

import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.SKIPPED;
import static io.qameta.allure.entity.Status.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

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
        List<TestCaseResult> testCases = process("simple.json", "cucumber-simple.json");
        assertThat(testCases)
                .isNotNull()
                .hasSize(5)
                .extracting(TestCaseResult::getStatus)
                .contains(FAILED, PASSED, SKIPPED, UNKNOWN);
    }

    @Test
    public void readEmptyJsonFile() throws Exception {
        List<TestCaseResult> results = process("empty.json", "cucumber-empty.json");

        assertThat(results)
                .isEmpty();
    }

    @Test
    public void readInvalidJsonFile() throws Exception {
        List<TestCaseResult> results = process("invalid.json", "cucumber-invalid.json");

        assertThat(results)
                .isEmpty();
    }

    @Test
    public void readJsonWithAttachment() throws Exception {
        List<TestCaseResult> results = process("complex.json", "cucumber-complex.json");
        assertThat(results)
                .isNotNull()
                .hasSize(4)
                .extracting(TestCaseResult::getTestStage)
                .flatExtracting(StageResult::getSteps)
                .hasSize(21)
                .flatExtracting(Step::getAttachments)
                .hasSize(8)
                .extracting(Attachment::getName)
                .contains("embedding_-115657502.image", "embedding_61853345.png");
    }

    @Test
    public void readSeveralJsonFiles() throws Exception {
        List<TestCaseResult> testCases = process(
                "simple.json", "cucumber-simple.json",
                "complex.json", "cucumber-complex.json"
        );

        assertThat(testCases)
                .hasSize(9);
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
