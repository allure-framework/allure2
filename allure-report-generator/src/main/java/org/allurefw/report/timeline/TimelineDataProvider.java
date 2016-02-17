package org.allurefw.report.timeline;

import com.google.inject.Inject;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TimelineData;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class TimelineDataProvider implements ReportDataProvider {

    protected final TimelineData data;

    @Inject
    protected TimelineDataProvider(TimelineData data) {
        this.data = data;
    }

    @Override
    public Object provide() {
        return data;
    }

    @Override
    public String getFileName() {
        return "timeline.json";
    }
}
