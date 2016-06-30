package org.allurefw.report.entity;

import java.util.Optional;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithFailure {

    Failure getFailure();

    void setFailure(Failure failure);

    default void setFailure(String message, String trace) {
        setFailure(new Failure().withMessage(message).withTrace(trace));
    }

    default Optional<Failure> getFailureIfExists() {
        return Optional.ofNullable(getFailure());
    }
}
