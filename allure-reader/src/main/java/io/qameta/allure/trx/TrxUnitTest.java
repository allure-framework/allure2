package io.qameta.allure.trx;

import io.qameta.allure.entity.TestParameter;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
/* default */ class TrxUnitTest {

    public static final String PARAMETER_PREFIX = "Parameter:";

    private final String name;
    private final String executionId;
    private final String description;
    private final Map<String, String> properties;

    public Set<TestParameter> getParameters() {
        return getProperties().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(PARAMETER_PREFIX))
                .map(entry -> {
                    final String name = entry.getKey().substring(PARAMETER_PREFIX.length());
                    return new TestParameter().setName(name).setValue(entry.getValue());
                }).collect(Collectors.toSet());
    }
}
