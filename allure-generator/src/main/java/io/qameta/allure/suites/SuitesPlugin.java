package io.qameta.allure.suites;

import io.qameta.allure.CommonCsvExportAggregator;
import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.csv.CsvExportSuite;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.PARENT_SUITE;
import static io.qameta.allure.entity.LabelName.SUB_SUITE;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.Statistic.comparator;
import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;
import static io.qameta.allure.tree.TreeUtils.calculateStatisticByLeafs;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * Plugin that generates data for Suites tab.
 *
 * @since 2.0
 */
public class SuitesPlugin extends CompositeAggregator implements Widget {

    private static final String SUITES = "suites";

    /** Name of the json file. */
    protected static final String JSON_FILE_NAME = "suites.json";

    /** Name of the csv file. */
    protected static final String CSV_FILE_NAME = "suites.csv";

    public SuitesPlugin() {
        super(Arrays.asList(new JsonAggregator(), new CsvExportAggregator()));
    }

    @SuppressWarnings("PMD.DefaultPackage")
    static /* default */ Tree<TestResult> getData(final List<LaunchResults> launchResults) {

        // @formatter:off
        final Tree<TestResult> xunit = new TestResultTree(
                SUITES,
            testResult -> groupByLabels(testResult, PARENT_SUITE, SUITE, SUB_SUITE)
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .sorted(comparingByTimeAsc())
                .forEach(xunit::add);
        return xunit;
    }

    @Override
    public Object getData(final Configuration configuration, final List<LaunchResults> launches) {
        final Tree<TestResult> data = getData(launches);
        final List<TreeWidgetItem> items = data.getChildren().stream()
                .filter(TestResultTreeGroup.class::isInstance)
                .map(TestResultTreeGroup.class::cast)
                .map(this::toWidgetItem)
                .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                .limit(10)
                .collect(Collectors.toList());
        return new TreeWidgetData().setItems(items).setTotal(data.getChildren().size());
    }

    @Override
    public String getName() {
        return SUITES;
    }

    private TreeWidgetItem toWidgetItem(final TestResultTreeGroup group) {
        return new TreeWidgetItem()
                .setUid(group.getUid())
                .setName(group.getName())
                .setStatistic(calculateStatisticByLeafs(group));
    }

    private static class JsonAggregator extends CommonJsonAggregator {

        JsonAggregator() {
            super(JSON_FILE_NAME);
        }

        @Override
        protected Tree<TestResult> getData(final List<LaunchResults> launchResults) {
            return SuitesPlugin.getData(launchResults);
        }
    }

    private static class CsvExportAggregator extends CommonCsvExportAggregator<CsvExportSuite> {

        CsvExportAggregator() {
            super(CSV_FILE_NAME, CsvExportSuite.class);
        }

        @Override
        protected List<CsvExportSuite> getData(final List<LaunchResults> launchesResults) {
            return launchesResults.stream()
                    .flatMap(launch -> launch.getResults().stream())
                    .map(CsvExportSuite::new).collect(Collectors.toList());
        }
    }
}
