package io.qameta.allure.jira.retrofit;

/**
 * Service exception when something wrong when execution call.
 */
public final class ServiceException extends RuntimeException {

    public ServiceException(final String message) {
        super(message);
    }

    public ServiceException(final String message, final Throwable e) {
        super(message, e);
    }

}
