package org.allurefw.report.entity;

import org.allurefw.report.Failure;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithFailure {

    Failure getFailure();

    void setFailure(Failure failure);

    default void setFailure(String message, String trace) {
        setFailure(new Failure().withMessage(message).withTrace(trace));
    }
}
