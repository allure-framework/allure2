package io.qameta.allure.validator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * @author charlie (Dmitry Baev).
 */
public class PortValidator implements IParameterValidator {

    private static final int MAX_PORT_VALUE = 65535;
    private static final String MESSAGE = "invalid port value. Should be an integer between 0 and 65535";

    @Override
    public void validate(final String name, final String value) throws ParameterException {
        try {
            final int port = Integer.parseInt(value);
            if (port < 0 || port > MAX_PORT_VALUE) {
                throw new ParameterException(MESSAGE);
            }
        } catch (NumberFormatException e) {
            throw new ParameterException(MESSAGE, e);
        }
    }
}
