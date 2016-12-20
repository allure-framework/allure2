package org.allurefw.report.severity;

import org.allurefw.report.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class SeverityPlugin extends AbstractPlugin {
    @Override
    protected void configure() {
        processor(SeverityProcessor.class);
    }
}
