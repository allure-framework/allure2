package org.allurefw.report.tms;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 18.02.16
 */
@Plugin(name = "tms-link")
public class TmsModule extends AbstractPlugin {

    @Override
    protected void configure() {
        processor(TmsLinkProcessor.class);
    }
}
