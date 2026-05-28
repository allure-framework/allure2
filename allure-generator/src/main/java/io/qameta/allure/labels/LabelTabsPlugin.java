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
package io.qameta.allure.labels;

import io.qameta.allure.CommonJsonAggregator2;
import io.qameta.allure.CompositeAggregator2;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.Tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * Generates tree data for label-based report tabs.
 */
public class LabelTabsPlugin extends CompositeAggregator2 {

    public static final String TAGS = "tags";

    public static final String ISSUES = "issues";

    public static final String TEST_TYPES = "test-types";

    private static final String JSON_EXTENSION = ".json";

    public LabelTabsPlugin() {
        super(
                Arrays.asList(
                        new LabelTreeAggregator(TAGS, TAGS + JSON_EXTENSION, LabelName.TAG),
                        new LabelTreeAggregator(ISSUES, ISSUES + JSON_EXTENSION, LabelName.ISSUE),
                        new LabelTreeAggregator(TEST_TYPES, TEST_TYPES + JSON_EXTENSION, LabelName.TEST_TYPE)
                )
        );
    }

    static Tree<TestResult> getData(final List<LaunchResults> launchResults,
                                    final String treeName,
                                    final LabelName labelName) {

        final Tree<TestResult> labels = new TestResultTree(
                treeName,
                testResult -> groupByLabels(testResult, labelName)
        );

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .filter(testResult -> !testResult.findAllLabels(labelName).isEmpty())
                .sorted(comparingByTimeAsc())
                .forEach(labels::add);
        return labels;
    }

    private static class LabelTreeAggregator extends CommonJsonAggregator2 {

        private final String treeName;

        private final LabelName labelName;

        LabelTreeAggregator(final String treeName, final String fileName, final LabelName labelName) {
            super(fileName);
            this.treeName = treeName;
            this.labelName = labelName;
        }

        @Override
        protected Object getData(final List<LaunchResults> launches) {
            return LabelTabsPlugin.getData(launches, treeName, labelName);
        }
    }
}
