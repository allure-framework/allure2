package io.qameta.allure.status;

import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.service.TestResultService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Status chart widget.
 *
 * @since 2.0
 */
public class StatusChartPlugin extends AbstractJsonAggregator {

    public StatusChartPlugin() {
        super("widgets", "status-chart.json");
    }

    @Override
    protected List<StatusChartData> getData(final ReportContext context,
                                            final TestResultService testResultService) {
        return testResultService.findAllTests().stream()
                .map(this::createData)
                .collect(Collectors.toList());
    }

    private StatusChartData createData(final TestResult result) {
        return new StatusChartData()
                .setId(result.getId())
                .setName(result.getName())
                .setStatus(result.getStatus())
                .setStart(result.getStart())
                .setStop(result.getStop())
                .setDuration(result.getDuration())
                .setSeverity(result.getExtraBlock("severity"));
    }
}
