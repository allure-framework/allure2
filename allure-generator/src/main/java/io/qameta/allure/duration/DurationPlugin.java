package io.qameta.allure.duration;

import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.service.TestResultService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Duration graph.
 *
 * @since 2.0
 */
public class DurationPlugin extends AbstractJsonAggregator {

    public DurationPlugin() {
        super("widgets", "duration.json");
    }

    @Override
    protected List<DurationGraphData> getData(final TestResultService service) {
        return service.findAllTests().stream()
                .map(this::createData)
                .collect(Collectors.toList());
    }

    private DurationGraphData createData(final TestResult result) {
        return new DurationGraphData()
                .setId(result.getId())
                .setName(result.getName())
                .setStatus(result.getStatus())
                .setStart(result.getStart())
                .setStop(result.getStop())
                .setDuration(result.getDuration())
                .setSeverity(result.getExtraBlock("severity"));
    }
}
