package io.qameta.allure.entity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Parameterizable {

    List<TestParameter> getParameters();

    default List<String> getParameterValues() {
        return getParameters().stream()
                .map(TestParameter::getValue)
                .collect(Collectors.toList());
    }
}
