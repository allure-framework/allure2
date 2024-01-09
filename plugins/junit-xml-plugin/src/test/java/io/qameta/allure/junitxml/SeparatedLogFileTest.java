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
package io.qameta.allure.junitxml;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SeparatedLogFileTest extends JunitXmlPluginTestBase {

    @Test
    void test1() throws Exception {
        process(
                "junitdata/TEST-test.SeparatedLogsTest.xml", "TEST-test.SeparatedLogsTest.xml",
                "junitdata/test.SeparatedLogsTest.txt", "test.SeparatedLogsTest.txt"
        );
        final int numberOfTestCases = 4;
        final ArgumentCaptor<Path> attachmentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(visitor, times(numberOfTestCases)).visitAttachmentFile(attachmentCaptor.capture());
        assertThat(attachmentCaptor.getAllValues())
                .hasSize(numberOfTestCases)
                .allMatch(path -> path.getFileName().startsWith(SeparatedLogFile.getPREFIX()));

        final String successContent = "l1\nl2\nl3\nl4_1 l4_2";
        final List<String> logFilesContents = List.of(successContent, successContent, "failureMessage1", "failureMessage2");
        IntStream.range(0, numberOfTestCases).forEach(
                index -> assertThat(attachmentCaptor.getAllValues().get(index))
                        .hasContent(logFilesContents.get(index))
        );
    }

}
