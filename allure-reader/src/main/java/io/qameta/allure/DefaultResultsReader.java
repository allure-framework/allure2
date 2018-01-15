package io.qameta.allure;

import io.qameta.allure.logging.LoggingResultsReader;

import java.util.Collections;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultResultsReader extends CompositeResultsReader {

    public DefaultResultsReader() {
        super(Collections.singletonList(new LoggingResultsReader()));
    }

}
