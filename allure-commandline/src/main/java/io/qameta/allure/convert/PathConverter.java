package io.qameta.allure.convert;

import com.beust.jcommander.IStringConverter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author charlie (Dmitry Baev).
 */
public class PathConverter implements IStringConverter<Path> {

    @Override
    public Path convert(final String value) {
        return Paths.get(value);
    }
}
