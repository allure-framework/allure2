package io.qameta.allure.util;

import io.qameta.allure.exception.PropertyNotFoundException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Properties;

/**
 * Utility methods for using properties.
 */
public final class PropertyUtils {

    private PropertyUtils() {
    }

    public static Optional<String> getProperty(final String key) {
        final Properties properties = new Properties();
        properties.putAll(System.getenv());
        return Optional.ofNullable(properties.getProperty(key)).filter(StringUtils::isNotBlank);
    }

    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    public static String requireProperty(final String key) {
        return getProperty(key).orElseThrow(() -> new PropertyNotFoundException(key));
    }

}
