package io.qameta.allure.ga;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Parameters for {@link GaPlugin}.
 *
 * @author eroshnkoam
 */
@Data
@Accessors(chain = true)
public class GaParameters {

    private String allureVersion;

    private String executorType;

    private String language;

    private String framework;

    private long resultsCount;

    private String resultsFormat;

}
