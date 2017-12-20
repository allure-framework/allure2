package io.qameta.allure.entity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Parameterizable {

    Set<TestParameter> getParameters();

    default List<String> getParameterValues() {
        return getParameters().stream()
                .map(TestParameter::getValue)
                .collect(Collectors.toList());
    }
}
