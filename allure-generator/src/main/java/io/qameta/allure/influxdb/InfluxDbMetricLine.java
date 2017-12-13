package io.qameta.allure.influxdb;

import io.qameta.allure.metric.MetricLine;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class InfluxDbMetricLine implements MetricLine, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String key;
    private final String value;
    private final long timestamp;

    @Override
    public String asString() {
        return String.format("%s %s=%s %d",
                getName(),
                normalize(getKey()),
                getValue(),
                getTimestamp()
        );
    }

    public static String normalize(final String string) {
        return string.toLowerCase().replaceAll("\\s+", "_");
    }
}
