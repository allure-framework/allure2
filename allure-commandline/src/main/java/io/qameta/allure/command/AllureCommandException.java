package io.qameta.allure.command;

/**
 * The exception indicates about some problems during Allure commandline execution.
 *
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
public class AllureCommandException extends RuntimeException {

    public AllureCommandException(final String message) {
        super(message);
    }

    public AllureCommandException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
