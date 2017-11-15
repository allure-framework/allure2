package io.qameta.allure.prometheus;

import io.qameta.allure.metric.MetricLine;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class PrometheusMetricLine implements MetricLine {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String key;
    private final String value;

    @Override
    public String asString() {
        return String.format("%s_%s %s",
                getName(),
                normalize(getKey()),
                getValue()
        );
    }

    public static String normalize(final String string) {
        return string.toLowerCase().replaceAll("\\s+", "_");
    }
}
