package io.qameta.allure.junit;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
/* default */ class JunitTestSuite {

    protected String name;
    protected String hostname;
    protected Long timestamp;

}
