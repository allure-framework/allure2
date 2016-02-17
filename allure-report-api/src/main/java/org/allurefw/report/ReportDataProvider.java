package org.allurefw.report;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */

//TODO typed? Maybe we should use generics?
public interface ReportDataProvider {

    Object provide();

    String getFileName();

    static ReportDataProvider provider(String name, Object object) {
        return new ReportDataProvider() {
            @Override
            public Object provide() {
                return object;
            }

            @Override
            public String getFileName() {
                return name;
            }
        };
    }
}
