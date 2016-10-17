package org.allurefw.report.entity;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WithParametersNames {

    List<String> getParametersNames();

    void setParametersNames(List<String> parameterNames);

    default void updateParametersNames(List<Parameter> parameters) {
        List<String> updated = concat(getParametersNames().stream(), parameters.stream().map(Parameter::getName))
                .distinct()
                .collect(Collectors.toList());
        setParametersNames(updated);
    }
}
