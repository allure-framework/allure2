package org.allurefw.report;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public interface ReportDataProvider {

    Object provide();

    String getFileName();

}
