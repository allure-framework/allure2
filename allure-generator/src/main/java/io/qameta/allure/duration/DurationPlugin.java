package io.qameta.allure.duration;

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
public class DurationPlugin extends CommonWidgetAggregator {

    public DurationPlugin() {
        super("duration.json");
    }

    @Override
    public WidgetCollection<DurationData> getData(final Configuration configuration,
                                                  final List<LaunchResults> launches) {
        List<DurationData> dataList = this.getData(launches);
        return new WidgetCollection<>(dataList.size(), dataList);
    }

    private List<DurationData> getData(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .flatMap(launch -> launch.getResults().stream())
                .map(this::createData)
                .collect(Collectors.toList());
    }

    private DurationData createData(final TestResult result) {
        return new DurationData()
                .setUid(result.getUid())
                .setName(result.getName())
                .setStatus(result.getStatus())
                .setTime(result.getTime())
                .setSeverity(result.getExtraBlock("severity"));
    }
}
