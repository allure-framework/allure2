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
package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.MarkdownContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Plugin that converts descriptions from markdown to html.
 *
 * @since 2.0
 */
public class MarkdownDescriptionsPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        configuration.getContext(MarkdownContext.class)
                .ifPresent(markdownContext -> processDescriptions(launchesResults, markdownContext));
    }

    private void processDescriptions(final List<LaunchResults> launches, final MarkdownContext context) {
        launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .filter(result -> isEmpty(result.getDescriptionHtml()) && !isEmpty(result.getDescription()))
                .forEach(result -> {
                    final String html = context.getValue().apply(result.getDescription());
                    result.setDescriptionHtml(html);
                });
    }

    private static boolean isEmpty(final String string) {
        return Objects.isNull(string) || string.isEmpty();
    }

}
