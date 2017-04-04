package io.qameta.allure.validator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author charlie (Dmitry Baev).
 */
public class DirectoryExistsValidator implements IParameterValidator {

    @Override
    public void validate(final String name, final String value) throws ParameterException {
        if (!Files.isDirectory(Paths.get(value))) {
            throw new ParameterException(String.format(
                    "invalid %s parameter: directory %s should exists",
                    name,
                    value
            ));
        }
    }
}
