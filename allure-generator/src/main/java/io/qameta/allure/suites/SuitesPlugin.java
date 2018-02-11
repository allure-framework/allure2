package io.qameta.allure.suites;

import io.qameta.allure.AbstractCsvExportAggregator;
import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.service.TestResultService;
import io.qameta.allure.tree.TestResultGroupNode;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.PARENT_SUITE;
import static io.qameta.allure.entity.LabelName.SUB_SUITE;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.Statistic.comparator;
import static io.qameta.allure.tree.TreeUtils.calculateStatisticByLeafs;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * Plugin that generates data for Suites tab.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.UseUtilityClass")
public class SuitesPlugin extends CompositeAggregator {

    private static final String SUITES = "suites";

    /**
     * Name of the json file.
     */
    protected static final String JSON_FILE_NAME = "suites.json";

    /**
     * Name of the csv file.
     */
    protected static final String CSV_FILE_NAME = "suites.csv";

    public SuitesPlugin() {
        super(new HashSet<>(Arrays.asList(
                new JsonAggregator(), new CsvExportAggregator(), new WidgetAggregator()
        )));
    }

    @SuppressWarnings("PMD.DefaultPackage")
    static /* default */ TestResultTree getData(final TestResultService service) {

        // @formatter:off
        final TestResultTree xunit = new TestResultTree(
                SUITES,
            testResult -> groupByLabels(testResult, PARENT_SUITE, SUITE, SUB_SUITE)
        );
        // @formatter:on

        service.findAllTests()
                .forEach(xunit::add);
        return xunit;
    }

    private static class JsonAggregator extends AbstractJsonAggregator {

        JsonAggregator() {
            super(JSON_FILE_NAME);
        }

        @Override
        protected TestResultTree getData(final ReportContext context,
                                         final TestResultService service) {
            return SuitesPlugin.getData(service);
        }
    }

    private static class CsvExportAggregator extends AbstractCsvExportAggregator<CsvExportSuite> {

        CsvExportAggregator() {
            super(CSV_FILE_NAME, CsvExportSuite.class);
        }

        @Override
        protected List<CsvExportSuite> getData(final TestResultService service) {
            return service.findAllTests().stream()
                    .map(CsvExportSuite::new)
                    .collect(Collectors.toList());
        }
    }

    private static class WidgetAggregator extends AbstractJsonAggregator {

        WidgetAggregator() {
            super("widgets", JSON_FILE_NAME);
        }

        @Override
        protected Object getData(final ReportContext context,
                                 final TestResultService service) {
            final TestResultTree data = SuitesPlugin.getData(service);
            final List<TreeWidgetItem> items = data.getGroups().stream()
                    .map(WidgetAggregator::toWidgetItem)
                    .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            return new TreeWidgetData().setItems(items).setTotal(data.getGroups().size());
        }

        private static TreeWidgetItem toWidgetItem(final TestResultGroupNode groupNode) {
            return new TreeWidgetItem()
                    .setUid(groupNode.getUid())
                    .setName(groupNode.getName())
                    .setStatistic(calculateStatisticByLeafs(groupNode));
        }
    }
}
