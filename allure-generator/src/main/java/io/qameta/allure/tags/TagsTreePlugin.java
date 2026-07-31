/*
 *  Copyright 2016-2026 Qameta Software Inc
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

import io.qameta.allure.CommonJsonAggregator2;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.DefaultTreeLayer;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.Tree;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;

/**
 * Plugin that generates data for Tags tab.
 */
public class TagsTreePlugin extends CommonJsonAggregator2 {

    protected static final String JSON_FILE_NAME = "tags.json";

    public TagsTreePlugin() {
        super(JSON_FILE_NAME);
    }

    static Tree<TestResult> getTagData(final List<LaunchResults> launchResults) {
        final Tree<TestResult> tags = new TestResultTree(
                TagsPlugin.TAGS_BLOCK_NAME,
                result -> {
                    final Set<String> values = result.getExtraBlock(
                            TagsPlugin.TAGS_BLOCK_NAME,
                            Collections.emptySet()
                    );
                    if (values.isEmpty()) {
                        return Collections.emptyList();
                    }
                    return Collections.singletonList(
                            new DefaultTreeLayer(
                                    values.stream().sorted().collect(Collectors.toList())
                            )
                    );
                }
        );

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .filter(TagsTreePlugin::hasTags)
                .sorted(comparingByTimeAsc())
                .forEach(tags::add);
        return tags;
    }

    private static boolean hasTags(final TestResult result) {
        final Set<String> values = result.getExtraBlock(
                TagsPlugin.TAGS_BLOCK_NAME,
                Collections.emptySet()
        );
        return !values.isEmpty();
    }

    @Override
    protected Tree<TestResult> getData(final List<LaunchResults> launches) {
        return getTagData(launches);
    }
}
