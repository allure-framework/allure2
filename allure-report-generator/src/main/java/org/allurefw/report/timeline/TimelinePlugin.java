package org.allurefw.report.timeline;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "timeline")
public class TimelinePlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(TimelineAggregator.class);
    }

}
