package org.allurefw.report;

import java.io.Serializable;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 05.03.16
 */
public interface ReportData<T extends Serializable> {

    T getData();

}
