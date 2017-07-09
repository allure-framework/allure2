package io.qameta.allure.entity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WithParameters {

    List<Parameter> getParameters();

    default List<String> getParameterValues() {
        return getParameters().stream()
                .map(Parameter::getValue)
                .collect(Collectors.toList());
    }
}
