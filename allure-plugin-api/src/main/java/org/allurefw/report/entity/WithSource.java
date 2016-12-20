package org.allurefw.report.entity;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 17.02.16
 */
public interface WithSource extends WithUid {

    default String getSource() {
        return getUid() + ".json";
    }
}
