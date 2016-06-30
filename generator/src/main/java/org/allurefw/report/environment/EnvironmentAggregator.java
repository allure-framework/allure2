package org.allurefw.report.environment;

import org.allurefw.report.Aggregator;
import org.allurefw.report.EnvironmentData;
import org.allurefw.report.EnvironmentParameter;
import org.allurefw.report.entity.TestCaseResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * @author charlie (Dmitry Baev).
 */
public class EnvironmentAggregator implements Aggregator<EnvironmentData> {

    @Override
    public Supplier<EnvironmentData> supplier() {
        return EnvironmentData::new;
    }

    @Override
    public BinaryOperator<EnvironmentData> combiner() {
        return (first, second) -> first.withParameters(second.getParameters());
    }

    @Override
    public BiConsumer<EnvironmentData, TestCaseResult> accumulator() {
        return (data, identity) -> identity.getEnvironment().forEach(item -> {
            Optional<EnvironmentParameter> parameter = data.getParameters().stream()
                    .filter(param -> Objects.equals(item.getName(), param.getName()))
                    .findAny();
            if (parameter.isPresent()) {
                EnvironmentParameter param = parameter.get();
                HashSet<String> newValues = new HashSet<>(param.getValues());
                newValues.add(item.getValue());
                param.setValues(new ArrayList<>(newValues));
            } else {
                data.getParameters().add(new EnvironmentParameter()
                        .withName(item.getName())
                        .withValues(item.getValue()));
            }
        });
    }
}
