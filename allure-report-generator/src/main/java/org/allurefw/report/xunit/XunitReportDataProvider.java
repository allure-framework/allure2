package org.allurefw.report.xunit;

import com.google.inject.Inject;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.XunitData;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class XunitReportDataProvider implements ReportDataProvider {

    protected final XunitData data;

    @Inject
    protected XunitReportDataProvider(XunitData data) {
        this.data = data;
    }

    @Override
    public Object provide() {
        return data;
    }

    @Override
    public String getFileName() {
        return "xunit.json";
    }
}
