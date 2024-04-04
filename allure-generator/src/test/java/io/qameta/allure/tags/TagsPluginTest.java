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
package io.qameta.allure.tags;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;

/**
 * @author charlie (Dmitry Baev).
 */
class TagsPluginTest {

    @Test
    void shouldAddTagsFromLabels() {
        final TestResult testResult = new TestResult()
                .setLabels(Arrays.asList(
                        new Label()
                                .setName("not a tag")
                                .setValue("some value"),
                        new Label()
                                .setName("tag")
                                .setValue("first"),
                        new Label()
                                .setName("feature")
                                .setValue("Auth"),
                        new Label()
                                .setName("tag")
                                .setValue("second"),
                        new Label()
                                .setName("tag")
                                .setValue("third")
                ));

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(Set.of(testResult), Map.of(), Map.of())
        );

        final Configuration configuration = ConfigurationBuilder.empty().build();

        final ReportStorage storage = mock();

        new TagsPlugin().aggregate(
                configuration,
                launchResults,
                storage
        );

        assertThat(testResult.<Set<String>>getExtraBlock(TagsPlugin.TAGS_BLOCK_NAME))
                .containsExactlyInAnyOrder(
                        "first",
                        "second",
                        "third"
                );
    }

    @Test
    void shouldRemoveDuplicateTags() {
        final TestResult testResult = new TestResult()
                .setLabels(Arrays.asList(
                        new Label()
                                .setName("not a tag")
                                .setValue("some value"),
                        new Label()
                                .setName("tag")
                                .setValue("first"),
                        new Label()
                                .setName("feature")
                                .setValue("Auth"),
                        new Label()
                                .setName("tag")
                                .setValue("first"),
                        new Label()
                                .setName("tag")
                                .setValue("second"),
                        new Label()
                                .setName("tag")
                                .setValue("    first    "),
                        new Label()
                                .setName("tag")
                                .setValue("third")
                ));

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(Set.of(testResult), Map.of(), Map.of())
        );

        final Configuration configuration = ConfigurationBuilder.empty().build();

        final ReportStorage storage = mock();

        new TagsPlugin().aggregate(
                configuration,
                launchResults,
                storage
        );

        assertThat(testResult.<Set<String>>getExtraBlock(TagsPlugin.TAGS_BLOCK_NAME))
                .containsExactlyInAnyOrder(
                        "first",
                        "second",
                        "third"
                );
    }

    @Test
    void shouldTrimTagNames() {
        final TestResult testResult = new TestResult()
                .setLabels(Arrays.asList(
                        new Label()
                                .setName("not a tag")
                                .setValue("some value"),
                        new Label()
                                .setName("tag")
                                .setValue(" first  \n "),
                        new Label()
                                .setName("feature")
                                .setValue("Auth"),
                        new Label()
                                .setName("tag")
                                .setValue("  second"),
                        new Label()
                                .setName("tag")
                                .setValue("third")
                ));

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(Set.of(testResult), Map.of(), Map.of())
        );

        final Configuration configuration = ConfigurationBuilder.empty().build();

        final ReportStorage storage = mock();

        new TagsPlugin().aggregate(
                configuration,
                launchResults,
                storage
        );

        assertThat(testResult.<Set<String>>getExtraBlock(TagsPlugin.TAGS_BLOCK_NAME))
                .containsExactlyInAnyOrder(
                        "first",
                        "second",
                        "third"
                );
    }

    @Test
    void shouldParseLabelsWithoutName() {
        final TestResult testResult = new TestResult()
                .setLabels(Arrays.asList(
                        new Label()
                                .setValue("some value"),
                        new Label()
                                .setName("tag")
                                .setValue("first"),
                        new Label()
                                .setName("feature")
                                .setValue("Auth"),
                        new Label()
                                .setName("tag")
                                .setValue("second"),
                        new Label()
                                .setName("tag")
                                .setValue("third")
                ));

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(Set.of(testResult), Map.of(), Map.of())
        );

        final Configuration configuration = ConfigurationBuilder.empty().build();

        final ReportStorage storage = mock();

        new TagsPlugin().aggregate(
                configuration,
                launchResults,
                storage
        );

        assertThat(testResult.<Set<String>>getExtraBlock(TagsPlugin.TAGS_BLOCK_NAME))
                .containsExactlyInAnyOrder(
                        "first",
                        "second",
                        "third"
                );
    }

    @Test
    void shouldParseLabelsWithoutValue() {
        final TestResult testResult = new TestResult()
                .setLabels(Arrays.asList(
                        new Label()
                                .setName("tag"),
                        new Label()
                                .setName("feature"),
                        new Label()
                                .setName("tag")
                                .setValue("second"),
                        new Label()
                                .setName("tag")
                                .setValue("third")
                ));

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(Set.of(testResult), Map.of(), Map.of())
        );

        final Configuration configuration = ConfigurationBuilder.empty().build();

        final ReportStorage storage = mock();

        new TagsPlugin().aggregate(
                configuration,
                launchResults,
                storage
        );

        assertThat(testResult.<Set<String>>getExtraBlock(TagsPlugin.TAGS_BLOCK_NAME))
                .containsExactlyInAnyOrder(
                        "second",
                        "third"
                );
    }

    @Test
    void shouldAddMetaTags() {
        final TestResult testResult = new TestResult()
                .setLabels(Arrays.asList(
                        new Label()
                                .setName("not a tag")
                                .setValue("some value"),
                        new Label()
                                .setName("tag")
                                .setValue("first"),
                        new Label()
                                .setName("feature")
                                .setValue("Auth"),
                        new Label()
                                .setName("tag")
                                .setValue("second"),
                        new Label()
                                .setName("tag")
                                .setValue("@allure.label.story=Some_story"),
                        new Label()
                                .setName("tag")
                                .setValue("@allure.label.parentSuite:Regression"),
                        new Label()
                                .setName("tag")
                                .setValue("@allure.label.suite:Search_Articles"),
                        new Label()
                                .setName("tag")
                                .setValue("allure.label.subSuite=Mobile")
                ));

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(Set.of(testResult), Map.of(), Map.of())
        );

        final Configuration configuration = ConfigurationBuilder.empty().build();

        final ReportStorage storage = mock();

        new TagsPlugin().aggregate(
                configuration,
                launchResults,
                storage
        );

        assertThat(testResult.getLabels())
                .extracting(Label::getName, Label::getValue)
                .containsExactlyInAnyOrder(
                        tuple("not a tag", "some value"),
                        tuple("tag", "first"),
                        tuple("feature", "Auth"),
                        tuple("tag", "second"),
                        tuple("tag", "@allure.label.story=Some_story"),
                        tuple("tag", "@allure.label.parentSuite:Regression"),
                        tuple("tag", "@allure.label.suite:Search_Articles"),
                        tuple("tag", "allure.label.subSuite=Mobile"),
                        tuple("story", "Some story"),
                        tuple("parentSuite", "Regression"),
                        tuple("suite", "Search Articles"),
                        tuple("subSuite", "Mobile")
                );
    }
}
