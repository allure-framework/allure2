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

import io.qameta.allure.Aggregator2;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class TagsPlugin implements Aggregator2 {

    private static final Pattern LABEL_TAG = Pattern.compile("^@?allure\\.label\\.(?<name>.+)[:=](?<value>.+)$");

    public static final String TAGS_BLOCK_NAME = "tags";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage reportStorage) {
        launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(result -> {

                    final Set<String> tags = result.findAllLabels(LabelName.TAG, Collectors.toSet());

                    final List<Label> extraLabels = tags.stream()
                            .filter(Objects::nonNull)
                            .filter(tag -> LABEL_TAG.matcher(tag).matches())
                            .map(TagsPlugin::createFromTag)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    if (!extraLabels.isEmpty()) {
                        final List<Label> labels = new ArrayList<>(result.getLabels());
                        labels.addAll(extraLabels);
                        result.setLabels(labels);
                    }

                    final Set<String> filteredTags = tags.stream()
                            .filter(Objects::nonNull)
                            .filter(tag -> !LABEL_TAG.matcher(tag).matches())
                            .map(String::trim)
                            .filter(s -> !"".equals(s))
                            .collect(Collectors.toSet());

                    result.addExtraBlock(TAGS_BLOCK_NAME, new HashSet<>(filteredTags));
                });
    }

    public static Optional<Label> createFromTag(final String tag) {
        final Matcher label = LABEL_TAG.matcher(tag);
        if (label.matches()) {
            final String name = label.group("name");
            final String value = label.group("value");
            return Optional.of(new Label()
                    .setName(name)
                    .setValue(value.replace("_", " ")));
        }
        return Optional.empty();
    }
}
