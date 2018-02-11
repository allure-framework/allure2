package io.qameta.allure.severity;

import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.Aggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.service.TestResultService;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.SEVERITY;

/**
 * Plugin that adds severity information to tests results.
 *
 * @since 2.0
 */
public class SeverityPlugin extends CompositeAggregator {

    /**
     * Name of the json file.
     */
    protected static final String JSON_FILE_NAME = "severity.json";

    public SeverityPlugin() {
        super(new HashSet<>(Arrays.asList(
                new SeverityAggregator(), new WidgetAggregator()
        )));
    }

    private static class SeverityAggregator implements Aggregator {

        @Override
        public void aggregate(final ReportContext context,
                              final TestResultService service,
                              final Path outputDirectory) {
            service.findAllTests()
                    .forEach(this::setSeverityLevel);

        }

        private void setSeverityLevel(final TestResult result) {
            final SeverityLevel severityLevel = result.findOneLabel(SEVERITY)
                    .flatMap(SeverityLevel::fromValue)
                    .orElse(SeverityLevel.NORMAL);
            result.addExtraBlock("severity", severityLevel);
        }
    }

    private static class WidgetAggregator extends AbstractJsonAggregator {

        WidgetAggregator() {
            super("widgets", JSON_FILE_NAME);
        }

        @Override
        protected List<SeverityData> getData(final ReportContext context,
                                             final TestResultService service) {
            return service.findAllTests().stream()
                    .map(this::createData)
                    .collect(Collectors.toList());
        }

        private SeverityData createData(final TestResult result) {
            return new SeverityData()
                    .setId(result.getId())
                    .setName(result.getName())
                    .setStatus(result.getStatus())
                    .setStart(result.getStart())
                    .setStop(result.getStop())
                    .setDuration(result.getDuration())
                    .setSeverity(result.getExtraBlock("severity"));
        }
    }
}
