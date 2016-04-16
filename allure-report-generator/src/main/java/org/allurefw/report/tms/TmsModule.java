package org.allurefw.report.tms;

import org.allurefw.report.AbstractPlugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 18.02.16
 */
public class TmsModule extends AbstractPlugin {

    @Override
    protected void configure() {
        processor(TmsLinkProcessor.class);
    }
}
