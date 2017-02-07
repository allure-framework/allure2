package io.qameta.allure.entity;

import java.util.Optional;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithTestCaseInfo extends WithName, WithTime, WithStatus, WithUid, WithStatusDetails {

    default TestCaseInfo toInfo() {
        boolean isFlaky = Optional.ofNullable(getStatusDetails())
                .map(StatusDetails::isFlaky)
                .orElse(false);
        return new TestCaseInfo()
                .withUid(getUid())
                .withName(getName())
                .withTime(getTime())
                .withStatus(getStatus())
                .withFlaky(isFlaky);
    }

}
