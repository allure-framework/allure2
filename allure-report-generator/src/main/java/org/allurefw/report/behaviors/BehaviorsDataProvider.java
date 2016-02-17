package org.allurefw.report.behaviors;

import com.google.inject.Inject;
import org.allurefw.report.BehaviorData;
import org.allurefw.report.ReportDataProvider;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class BehaviorsDataProvider implements ReportDataProvider {

    protected final BehaviorData data;

    @Inject
    protected BehaviorsDataProvider(BehaviorData data) {
        this.data = data;
    }

    @Override
    public Object provide() {
        return data;
    }

    @Override
    public String getFileName() {
        return "behaviors.json";
    }
}
