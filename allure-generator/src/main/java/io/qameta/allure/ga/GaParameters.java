package io.qameta.allure.ga;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * eroshenkoam
 * 06.08.17
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
