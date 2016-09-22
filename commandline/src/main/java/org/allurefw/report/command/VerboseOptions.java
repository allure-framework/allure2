package org.allurefw.report.command;

import com.github.rvesse.airline.annotations.Option;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

/**
 * @author charlie (Dmitry Baev).
 */
public class VerboseOptions {

    @Option(
            name = {"-v", "--verbose"},
            description = "Switch on the verbose mode.")
    protected boolean verbose = false;

    @Option(
            name = {"-q", "--quiet"},
            description = "Switch on the quiet mode.")
    protected boolean quiet = false;

    /**
     * Returns true if silent mode is enabled, false otherwise.
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Returns true if verbose mode is enabled, false otherwise.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Configure logger with needed level {@link #getLogLevel()}.
     */
    public void configureLogLevel() {
        LogManager.getRootLogger().setLevel(getLogLevel());
    }

    /**
     * Get log level depends on provided client parameters such as verbose and quiet.
     */
    private Level getLogLevel() {
        return isQuiet() ? Level.OFF : isVerbose() ? Level.DEBUG : Level.INFO;
    }
}
