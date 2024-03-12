/*
 *  Copyright 2016-2024 Qameta Software Inc
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
package io.qameta.allure.entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev baev@qameta.io
 * Date: 31.01.16
 */
public interface WithLabels {

    List<Label> getLabels();

    void setLabels(List<Label> labels);

    default <T> T findAll(final LabelName name, final Collector<String, ?, T> collector) {
        return findAll(name.value(), collector);
    }

    default <T> T findAll(final String name, final Collector<String, ?, T> collector) {
        return getLabels().stream()
                .filter(label -> name.equals(label.getName()))
                .map(Label::getValue)
                .collect(collector);
    }

    default List<String> findAll(final LabelName name) {
        return findAll(name, Collectors.toList());
    }

    default List<String> findAll(final String name) {
        return findAll(name, Collectors.toList());
    }

    default Optional<String> findOne(final LabelName name) {
        return findOne(name.value());
    }

    default Optional<String> findOne(final String name) {
        return getLabels().stream()
                .filter(label -> name.equals(label.getName()))
                .map(Label::getValue)
                .findAny();
    }

    default void addLabelIfNotExists(final LabelName name, final String value) {
        addLabelIfNotExists(name.value(), value);
    }

    default void addLabelIfNotExists(final String name, final String value) {
        if (value == null || name == null) {
            return;
        }
        final Optional<String> any = getLabels().stream()
                .map(Label::getName)
                .filter(name::equals)
                .findAny();
        if (!any.isPresent()) {
            addLabel(name, value);
        }
    }

    default void addLabel(final String name, final String value) {
        getLabels().add(new Label().setName(name).setValue(value));
    }
}
