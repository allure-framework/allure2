package io.qameta.allure.entity;

/**
 * @author Dmitry Baev baev@qameta.io
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
