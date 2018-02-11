package io.qameta.allure.summary;

import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.service.TestResultService;

/**
 * Plugins generates Summary widget.
 *
 * @since 2.0
 */
public class SummaryPlugin extends AbstractJsonAggregator {

    /**
     * Name of the json file.
     */
    protected static final String JSON_FILE_NAME = "summary.json";

    public SummaryPlugin() {
        super("widgets", JSON_FILE_NAME);
    }

    @Override
    protected SummaryData getData(final ReportContext context,
                                  final TestResultService testResultService) {
        final SummaryData data = new SummaryData()
                .setStatistic(new Statistic())
                .setTime(new GroupTime())
                .setReportName(context.getProject().getName());

        testResultService.findAllTests()
                .forEach(result -> {
                    data.getStatistic().update(result);
                    data.getTime().update(result);
                });
        return data;
    }
}
