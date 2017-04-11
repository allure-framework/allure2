package io.qameta.allure.exception;

/**
 * Notified about missed context.
 *
 * @see io.qameta.allure.Context
 * @since 2.0
 */
public class ContextNotFoundException extends RuntimeException {

    /**
     * Creates an exception by given context type.
     *
     * @param contextType the type of context.
     */
    public ContextNotFoundException(final Class<?> contextType) {
        super(String.format("Required context not found: %s", contextType));
    }
}
