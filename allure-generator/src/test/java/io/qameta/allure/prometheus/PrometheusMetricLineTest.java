package io.qameta.allure.prometheus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * @author Anton Tsyganov (jenkl)
 */
@RunWith(Parameterized.class)
public class PrometheusMetricLineTest {
    private static final String METRIC_NAME = "launch";
    private static final String METRIC_KEY = "status passed";
    private static final String METRIC_VALUE = "300";

    private final String labels;
    private final String expectedMetric;

    public PrometheusMetricLineTest(String labels, String expectedMetric, String name) {
        this.labels = labels;
        this.expectedMetric = expectedMetric;
    }

    @Parameterized.Parameters(name = "prometheus metric {2}")
    public static String[][] data() {
        return new String[][] {
            {"evn=\"test\",suite=\"regression\"", "launch_status_passed{evn=\"test\",suite=\"regression\"} 300",
                "with labels"},
            {null, "launch_status_passed 300", "without labels"}
        };
    }

    @Test
    public void shouldReturnMetric() {
        PrometheusMetricLine prometheusMetric = new PrometheusMetricLine(METRIC_NAME, METRIC_KEY, METRIC_VALUE, labels);
        assertEquals(prometheusMetric.asString(), expectedMetric);
    }
}
