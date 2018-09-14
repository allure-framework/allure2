package io.qameta.allure.exception;

import io.qameta.allure.util.PropertyUtils;

/**
 * Notified about missed property.
 *
 * @see PropertyUtils
 * @since 2.0
 */
public class PropertyNotFoundException extends RuntimeException {

    /**
     * Creates an exception by given property key.
     *
     * @param key the key of property.
     */
    public PropertyNotFoundException(final String key) {
        super(String.format("Required property not found: %s", key));
    }

}
