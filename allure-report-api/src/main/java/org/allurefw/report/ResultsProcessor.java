package org.allurefw.report;

import java.nio.file.Path;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public interface ResultsProcessor {

    void process(Path resultDirectory);

    void setReportDataManager(ReportDataManager manager);

}
