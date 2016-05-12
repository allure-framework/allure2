package org.allurefw.report.command;

/**
 * The exception indicates about some problems during Allure commandline execution.
 *
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
public class AllureCommandException extends RuntimeException {

    public AllureCommandException(String message) {
        super(message);
    }

    public AllureCommandException(Throwable cause) {
        super(cause);
    }
}
