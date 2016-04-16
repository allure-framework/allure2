package org.allurefw.report.report;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
@Plugin(name = "report-info")
public class ReportModule extends AbstractPlugin {

    @Override
    protected void configure() {
//        reportDataBuilder("report.json").toProvider(ReportInfoDataProvider.class);
    }
}
