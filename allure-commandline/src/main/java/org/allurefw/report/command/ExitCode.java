package org.allurefw.report.command;

/**
 * @author etki
 */
public enum ExitCode {

    NO_ERROR(0),
    GENERIC_ERROR(1),
    ARGUMENT_PARSING_ERROR(127);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
