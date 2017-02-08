package io.qameta.allure.entity;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithStatusDetails {

    StatusDetails getStatusDetails();

    void setStatusDetails(StatusDetails details);

    default StatusDetails getStatusDetailsSafe() {
        if (Objects.isNull(getStatusDetails())) {
            setStatusDetails(new StatusDetails());
        }
        return getStatusDetails();
    }

    default void setStatusMessage(String message) {
        getStatusDetailsSafe().setMessage(message);
    }

    default void setStatusTrace(String trace) {
        getStatusDetailsSafe().setTrace(trace);
    }

    default Optional<String> getStatusMessage() {
        return Optional.ofNullable(getStatusDetails())
                .map(StatusDetails::getMessage);
    }
}
