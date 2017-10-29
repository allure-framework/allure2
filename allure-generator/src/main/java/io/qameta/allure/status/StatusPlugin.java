package io.qameta.allure.status;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Graph tab.
 *
 * @since 2.0
 */
public class StatusPlugin extends CommonJsonAggregator {

    public StatusPlugin() {
        super("widgets", "status.json");
    }

    @Override
    protected List<StatusData> getData(final List<LaunchResults> launchesResults) {
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
}
