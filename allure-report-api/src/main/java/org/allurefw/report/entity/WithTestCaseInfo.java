package org.allurefw.report.entity;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithTestCaseInfo extends WithName, WithTime, WithStatus, WithUid {

    default TestCaseInfo toInfo() {
        return new TestCaseInfo()
                .withUid(getUid())
                .withName(getName())
                .withTime(getTime())
                .withStatus(getStatus());
    }

}
