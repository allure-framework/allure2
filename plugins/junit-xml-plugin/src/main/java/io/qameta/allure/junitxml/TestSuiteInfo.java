package io.qameta.allure.junitxml;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
/* default */ class TestSuiteInfo {

    protected String name;
    protected String hostname;
    protected Long timestamp;

}
