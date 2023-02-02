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
package io.qameta.allure.idea;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdeaLinksPluginTest {

    private static final String TEST_CLASS = "io.qameta.allure.AllureTest";

    @Test
    void shouldExportTestResultToJira() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult testResult = new TestResult()
                .setLabels(Collections.singletonList(new Label().setName("testClass").setValue(TEST_CLASS)));

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final IdeaLinksPlugin jiraTestResultExportPlugin = new IdeaLinksPlugin(true, 63342);

        jiraTestResultExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );

        assertThat(testResult.getLinks()).hasSize(1);
        final Link link = testResult.getLinks().get(0);
        assertThat(link.getName()).isEqualTo("Open in Idea");
        assertThat(link.getType()).isEqualTo("idea");
        assertThat(link.getUrl()).contains(TEST_CLASS.replace(".", "/"));

    }

}
