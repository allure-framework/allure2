package io.qameta.allure.severity;

import io.qameta.allure.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class SeverityPlugin extends AbstractPlugin {
    @Override
    protected void configure() {
        processor(SeverityProcessor.class);
    }
}
