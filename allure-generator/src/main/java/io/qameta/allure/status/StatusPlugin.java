package io.qameta.allure.status;

import io.qameta.allure.CommonWidgetAggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Graph tab.
 *
 * @since 2.0
 */
public class StatusPlugin extends CommonWidgetAggregator {

    public StatusPlugin() {
        super("status.json");
    }

    private List<StatusData> getData(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .flatMap(launch -> launch.getResults().stream())
                .map(this::createData)
                .collect(Collectors.toList());
    }

    private StatusData createData(final TestResult result) {
        return new StatusData()
                .setUid(result.getUid())
                .setName(result.getName())
                .setStatus(result.getStatus())
                .setTime(result.getTime())
                .setSeverity(result.getExtraBlock("severity"));
    }

    @Override
    public WidgetCollection<StatusData> getData(Configuration configuration, List<LaunchResults> launches) {
        List<StatusData> dataList = this.getData(launches);
        return new WidgetCollection<>(dataList.size(), dataList);
    }
}
