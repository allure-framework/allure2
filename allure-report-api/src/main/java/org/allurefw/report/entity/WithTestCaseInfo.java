package org.allurefw.report.entity;

import org.allurefw.report.TestCaseInfo;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithTestCaseInfo extends WithName, WithTime, WithStatus {

    default TestCaseInfo toInfo() {
        return new TestCaseInfo()
                .withName(getName())
                .withTime(getTime())
                .withStatus(getStatus());
    }

}
