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
    private final String labels;

    @Override
    public String asString() {
        return String.format("%s_%s%s %s",
                getName(),
                normalize(getKey()),
                normalizeLabels(getLabels()),
                getValue()
        );
    }

    public static String normalize(final String string) {
        return string.toLowerCase().replaceAll("\\s+", "_");
    }

    private String normalizeLabels(final String labels) {
        return labels == null ? "" : "{" + labels + "}";
    }
}
