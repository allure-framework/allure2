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
package io.qameta.allure.markdown;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.MarkdownDescriptionsPlugin;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownAggregatorTest {

    private final Configuration configuration = new ConfigurationBuilder().useDefault().build();

    @Test
    void shouldNotFailIfEmptyResults(@TempDir final Path output) {
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();
        aggregator.aggregate(configuration, Collections.emptyList(), output);
    }

    @Test
    void shouldSkipResultsWithEmptyDescription(@TempDir final Path output) {
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult().setName("some");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly(null, null);
    }

    @Test
    void shouldSkipResultsWithNonEmptyDescriptionHtml(@TempDir final Path output) {
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setName("some")
                .setDescription("desc")
                .setDescriptionHtml("descHtml");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("desc", "descHtml");
    }

    @Test
    void shouldProcessDescription(@TempDir final Path output) {
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setName("some")
                .setDescription("desc");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("desc", "<p>desc</p>\n");
    }
}
