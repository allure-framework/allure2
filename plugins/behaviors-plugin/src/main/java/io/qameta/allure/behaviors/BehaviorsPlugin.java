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
package io.qameta.allure.behaviors;

import io.qameta.allure.CommonCsvExportAggregator;
import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.csv.CsvExportBehavior;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.EPIC;
import static io.qameta.allure.entity.LabelName.FEATURE;
import static io.qameta.allure.entity.LabelName.STORY;
import static io.qameta.allure.entity.Statistic.comparator;
import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;
import static io.qameta.allure.tree.TreeUtils.calculateStatisticByChildren;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * The plugin adds behaviors tab to the report.
 *
 * @since 2.0
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.UseUtilityClass"})
public class BehaviorsPlugin extends CompositeAggregator {

    protected static final String BEHAVIORS = "behaviors";

    protected static final String JSON_FILE_NAME = "behaviors.json";

    protected static final String CSV_FILE_NAME = "behaviors.csv";

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static final LabelName[] LABEL_NAMES = {EPIC, FEATURE, STORY};

    public BehaviorsPlugin() {
        super(Arrays.asList(
                new JsonAggregator(), new CsvExportAggregator(), new WidgetAggregator()
        ));
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static Tree<TestResult> getData(final List<LaunchResults> launchResults) {

        // @formatter:off
        final Tree<TestResult> behaviors = new TestResultTree(
            BEHAVIORS,
            testResult -> groupByLabels(testResult, LABEL_NAMES)
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .sorted(comparingByTimeAsc())
                .forEach(behaviors::add);
        return behaviors;
    }

    private static boolean isNotEmpty(final List<String> strings) {
        return !Objects.isNull(strings) && !strings.isEmpty();
    }

    /**
     * Generates tree data.
     */
    private static class JsonAggregator extends CommonJsonAggregator {

        JsonAggregator() {
            super(JSON_FILE_NAME);
        }

        @Override
        protected Tree<TestResult> getData(final List<LaunchResults> launches) {
            return BehaviorsPlugin.getData(launches);
        }
    }

    /**
     * Generates export data.
     */
    private static class CsvExportAggregator extends CommonCsvExportAggregator<CsvExportBehavior> {

        CsvExportAggregator() {
            super(CSV_FILE_NAME, CsvExportBehavior.class);
        }

        @Override
        protected List<CsvExportBehavior> getData(final List<LaunchResults> launchesResults) {
            final List<CsvExportBehavior> exportBehaviors = new ArrayList<>();
            launchesResults.stream().flatMap(launch -> launch.getResults().stream()).forEach(result -> {
                final Map<LabelName, List<String>> epicFeatureStoryMap = new HashMap<>();
                Arrays.asList(LABEL_NAMES).forEach(
                        label -> epicFeatureStoryMap.put(label, result.findAllLabels(label))
                );
                addTestResult(exportBehaviors, result, epicFeatureStoryMap);
            });
            return exportBehaviors;
        }

        private void addTestResult(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                   final Map<LabelName, List<String>> epicFeatureStoryMap) {
            if (epicFeatureStoryMap.isEmpty()) {
                addTestResultWithLabels(exportBehaviors, result, null, null, null);
            } else {
                addTestResultWithEpic(exportBehaviors, result, epicFeatureStoryMap);
            }
        }

        private void addTestResultWithEpic(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                           final Map<LabelName, List<String>> epicFeatureStoryMap) {
            if (isNotEmpty(epicFeatureStoryMap.get(EPIC))) {
                epicFeatureStoryMap.get(EPIC).forEach(
                        epic -> addTestResultWithFeature(exportBehaviors, result, epicFeatureStoryMap, epic)
                );
            } else {
                addTestResultWithFeature(exportBehaviors, result, epicFeatureStoryMap, null);
            }
        }

        private void addTestResultWithFeature(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                              final Map<LabelName, List<String>> epicFeatureStoryMap,
                                              final String epic) {
            if (isNotEmpty(epicFeatureStoryMap.get(FEATURE))) {
                epicFeatureStoryMap.get(FEATURE).forEach(
                        feature -> addTestResultWithStories(exportBehaviors, result, epicFeatureStoryMap, epic, feature)
                );
            } else {
                addTestResultWithStories(exportBehaviors, result, epicFeatureStoryMap, epic, null);
            }
        }

        private void addTestResultWithStories(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                              final Map<LabelName, List<String>> epicFeatureStoryMap,
                                              final String epic, final String feature) {
            if (isNotEmpty(epicFeatureStoryMap.get(STORY))) {
                epicFeatureStoryMap.get(STORY).forEach(
                        story -> addTestResultWithLabels(exportBehaviors, result, epic, feature, story)
                );
            } else {
                addTestResultWithLabels(exportBehaviors, result, epic, feature, null);
            }
        }

        private void addTestResultWithLabels(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                             final String epic, final String feature, final String story) {
            final Optional<CsvExportBehavior> behavior = exportBehaviors.stream()
                    .filter(exportBehavior -> exportBehavior.isPassed(epic, feature, story)).findFirst();
            if (behavior.isPresent()) {
                behavior.get().addTestResult(result);
            } else {
                final CsvExportBehavior exportBehavior = new CsvExportBehavior(epic, feature, story);
                exportBehavior.addTestResult(result);
                exportBehaviors.add(exportBehavior);
            }
        }
    }

    /**
     * Generates widget data.
     */
    protected static class WidgetAggregator extends CommonJsonAggregator {

        WidgetAggregator() {
            super(Constants.WIDGETS_DIR, JSON_FILE_NAME);
        }

        @Override
        public TreeWidgetData getData(final List<LaunchResults> launches) {
            final Tree<TestResult> data = BehaviorsPlugin.getData(launches);
            final List<TreeWidgetItem> items = data.getChildren().stream()
                    .filter(TestResultTreeGroup.class::isInstance)
                    .map(TestResultTreeGroup.class::cast)
                    .map(WidgetAggregator::toWidgetItem)
                    .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            return new TreeWidgetData().setItems(items).setTotal(data.getChildren().size());
        }

        private static TreeWidgetItem toWidgetItem(final TestResultTreeGroup group) {
            return new TreeWidgetItem()
                    .setUid(group.getUid())
                    .setName(group.getName())
                    .setStatistic(calculateStatisticByChildren(group));
        }
    }

}
