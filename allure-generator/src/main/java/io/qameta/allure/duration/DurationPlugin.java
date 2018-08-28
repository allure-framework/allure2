package io.qameta.allure.duration;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.severity.SeverityPlugin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Duration graph.
 *
 * @since 2.0
 */
public class DurationPlugin extends CommonJsonAggregator {

    public DurationPlugin() {
        super(Constants.WIDGETS_DIR, "duration.json");
    }

    @Override
    protected List<DurationData> getData(final List<LaunchResults> launchesResults) {
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
                .setSeverity(result.getExtraBlock(SeverityPlugin.SEVERITY_BLOCK_NAME));
    }
}
