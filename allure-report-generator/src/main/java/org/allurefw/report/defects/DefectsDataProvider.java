package org.allurefw.report.defects;

import com.google.inject.Inject;
import org.allurefw.report.DefectsData;
import org.allurefw.report.ReportDataProvider;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class DefectsDataProvider implements ReportDataProvider {

    protected final DefectsData data;

    @Inject
    protected DefectsDataProvider(DefectsData data) {
        this.data = data;
    }

    @Override
    public Object provide() {
        return data;
    }

    @Override
    public String getFileName() {
        return "defects.json";
    }
}
