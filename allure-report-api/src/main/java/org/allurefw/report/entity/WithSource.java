package org.allurefw.report.entity;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public interface WithSource extends WithUid {

    default String getSource() {
        return getUid() + ".json";
    }
}
