package io.qameta.allure.exception;

/**
 * @author charlie (Dmitry Baev).
 */
public class ContextNotFoundException extends RuntimeException {

    public ContextNotFoundException(final Class<?> contextType) {
        super(String.format("Required context not found: %s", contextType));
    }
}
