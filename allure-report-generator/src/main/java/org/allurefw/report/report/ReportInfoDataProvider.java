package org.allurefw.report.report;

import org.allurefw.report.ReportDataProvider;

import java.util.Collections;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class ReportInfoDataProvider implements ReportDataProvider {

    @Override
    public Object provide() {
        return Collections.singletonMap("name", "Allure Test Pack");
    }

    @Override
    public String getFileName() {
        return "report.json";
    }
}
