package io.qameta.allure.status;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Status chart widget.
 *
 * @since 2.0
 */
public class StatusChartPlugin extends CommonJsonAggregator {

    public StatusChartPlugin() {
        super("widgets", "status-chart.json");
    }

    @Override
    protected List<StatusChartData> getData(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .flatMap(launch -> launch.getResults().stream())
                .map(this::createData)
                .collect(Collectors.toList());
    }

    private StatusChartData createData(final TestResult result) {
        return new StatusChartData()
                .setUid(result.getUid())
                .setName(result.getName())
                .setStatus(result.getStatus())
                .setTime(result.getTime())
                .setSeverity(result.getExtraBlock("severity"));
    }
}
