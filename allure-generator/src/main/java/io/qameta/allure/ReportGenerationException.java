package io.qameta.allure;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 22.10.13
 *         <p/>
 *         Signals that an attempt to generate the reportData in specified directory has failed.
 */
public class ReportGenerationException extends RuntimeException {

    /**
     * Constructs the {@link ReportGenerationException} from given cause
     *
     * @param cause given {@link java.lang.Throwable} cause
     */
    public ReportGenerationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs the {@link ReportGenerationException} from given cause
     * and detail message.
     *
     * @param message the detail message.
     * @param cause   given {@link java.lang.Throwable} cause
     */
    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs the {@link ReportGenerationException} with specified detail message
     *
     * @param message the detail message.
     */
    public ReportGenerationException(String message) {
        super(message);
    }
}
