/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
    private final String className;
    private final String executionId;
    private final String description;
    private final Map<String, String> properties;

    public UnitTest(final String name, final String className, final String executionId,
                    final String description, final Map<String, String> properties) {
        this.name = name;
        this.className = className;
        this.executionId = executionId;
        this.description = description;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
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
