package io.qameta.allure.trx;

import io.qameta.allure.entity.Parameter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class UnitTest {

    public static final String PARAMETER_PREFIX = "Parameter:";
    private final String name;
    private final String executionId;
    private final String description;
    private final Map<String, String> properties;

    public UnitTest(final String name, final String executionId,
                    final String description, final Map<String, String> properties) {
        this.name = name;
        this.executionId = executionId;
        this.description = description;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<Parameter> getParameters() {
        return getProperties().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(PARAMETER_PREFIX))
                .map(entry -> {
                    final String name = entry.getKey().substring(PARAMETER_PREFIX.length());
                    return new Parameter().setName(name).setValue(entry.getValue());
                }).collect(Collectors.toList());
    }
}
