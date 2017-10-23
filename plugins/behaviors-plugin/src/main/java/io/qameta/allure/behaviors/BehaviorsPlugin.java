package io.qameta.allure.behaviors;

import io.qameta.allure.CommonCsvExportAggregator;
import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.csv.CsvExportBehavior;
import io.qameta.allure.csv.CsvExportCategory;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.EPIC;
import static io.qameta.allure.entity.LabelName.FEATURE;
import static io.qameta.allure.entity.LabelName.STORY;
import static io.qameta.allure.entity.Statistic.comparator;
import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;
import static io.qameta.allure.tree.TreeUtils.calculateStatisticByChildren;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;
import static java.util.Optional.ofNullable;

/**
 * The plugin adds behaviors tab to the report.
 *
 * @since 2.0
 */
public class BehaviorsPlugin extends CompositeAggregator implements Widget {

    public static final String BEHAVIORS = "behaviors";

    public static final String JSON_FILE_NAME = "behaviors.json";

    public static final String CSV_FILE_NAME = "behaviors.csv";

    private static final LabelName[] labelNames = new LabelName[] {EPIC, FEATURE, STORY};

    public BehaviorsPlugin() {
        super(Arrays.asList(new JsonAggregator(), new CsvExportAggregator()));
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static Tree<TestResult> getData(final List<LaunchResults> launchResults) {

        // @formatter:off
        final Tree<TestResult> behaviors = new TestResultTree(
            BEHAVIORS,
            testResult -> groupByLabels(testResult, labelNames)
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .sorted(comparingByTimeAsc())
                .forEach(behaviors::add);
        return behaviors;
    }

    @Override
    public Object getData(final Configuration configuration, final List<LaunchResults> launches) {
        final Tree<TestResult> data = getData(launches);
        final List<TreeWidgetItem> items = data.getChildren().stream()
                .filter(TestResultTreeGroup.class::isInstance)
                .map(TestResultTreeGroup.class::cast)
                .map(BehaviorsPlugin::toWidgetItem)
                .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                .limit(10)
                .collect(Collectors.toList());
        return new TreeWidgetData().setItems(items).setTotal(data.getChildren().size());
    }

    @Override
    public String getName() {
        return BEHAVIORS;
    }

    protected static TreeWidgetItem toWidgetItem(final TestResultTreeGroup group) {
        return new TreeWidgetItem()
                .setUid(group.getUid())
                .setName(group.getName())
                .setStatistic(calculateStatisticByChildren(group));
    }

    private static class JsonAggregator extends CommonJsonAggregator {

        JsonAggregator() {
            super(JSON_FILE_NAME);
        }

        @Override
        protected Tree<TestResult> getData(final List<LaunchResults> launchResults) {
            return BehaviorsPlugin.getData(launchResults);
        }
    }

    private static class CsvExportAggregator extends CommonCsvExportAggregator<CsvExportBehavior> {

        CsvExportAggregator() {
            super(CSV_FILE_NAME, CsvExportBehavior.class);
        }

        @Override
        protected List<CsvExportBehavior> getData(final List<LaunchResults> launchesResults) {
            final List<CsvExportBehavior> exportBehaviors = new ArrayList<>();
            launchesResults.stream().flatMap(launch -> launch.getResults().stream()).forEach(result -> {

                Map<LabelName, List<String>> epicFeatureStoryMap = new HashMap<>();
                Arrays.asList(labelNames).forEach(labelName ->
                        epicFeatureStoryMap.put(labelName, result.findAllLabels(labelName)));

                addTestResult(exportBehaviors, result, epicFeatureStoryMap);
            });
            return exportBehaviors;
        }

        private void addTestResult(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                   final Map<LabelName, List<String>> epicFeatureStoryMap) {
            if (epicFeatureStoryMap.isEmpty()) {
                addTestResult(exportBehaviors, result, null, null, null);
            } else {
                addTestResultWithEpic(exportBehaviors, result, epicFeatureStoryMap);
            }
        }

        private void addTestResultWithEpic(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                           final Map<LabelName, List<String>> epicFeatureStoryMap) {
            if (!CollectionUtils.isEmpty(epicFeatureStoryMap.get(EPIC))) {
                epicFeatureStoryMap.get(EPIC).forEach(epic ->
                        addTestResultWithFeature(exportBehaviors, result, epicFeatureStoryMap, epic)
                );
            } else {
                addTestResultWithFeature(exportBehaviors, result, epicFeatureStoryMap, null);
            }
        }

        private void addTestResultWithFeature(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                              final Map<LabelName, List<String>> epicFeatureStoryMap,
                                              final String epic) {
            if (!CollectionUtils.isEmpty(epicFeatureStoryMap.get(FEATURE))) {
                epicFeatureStoryMap.get(FEATURE).forEach(feature ->
                        addTestResultWithStories(exportBehaviors, result, epicFeatureStoryMap, epic, feature)
                );
            } else {
                addTestResultWithStories(exportBehaviors, result, epicFeatureStoryMap, epic, null);
            }
        }

        private void addTestResultWithStories(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                                 final Map<LabelName, List<String>> epicFeatureStoryMap,
                                                 final String epic, final String feature) {
            if (!CollectionUtils.isEmpty(epicFeatureStoryMap.get(STORY))) {
                epicFeatureStoryMap.get(STORY).forEach(story ->
                        addTestResult(exportBehaviors, result, epic, feature, story)
                );
            } else {
                addTestResult(exportBehaviors, result, epic, feature, null);
            }
        }

        private void addTestResult(final List<CsvExportBehavior> exportBehaviors, final TestResult result,
                                   final String epic, final String feature, final String story) {
            Optional<CsvExportBehavior> behavior = exportBehaviors.stream()
                    .filter(exportBehavior -> exportBehavior.isPassed(epic, feature, story)).findFirst();
            if (behavior.isPresent()) {
                behavior.get().addTestResult(result);
            } else {
                CsvExportBehavior exportBehavior = new CsvExportBehavior(epic, feature, story);
                exportBehavior.addTestResult(result);
                exportBehaviors.add(exportBehavior);
            }
        }
    }
}
