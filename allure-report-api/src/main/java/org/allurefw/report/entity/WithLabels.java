package org.allurefw.report.entity;

import org.allurefw.Label;
import org.allurefw.LabelName;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithLabels {

    List<Label> getLabels();

    void setLabels(List<Label> labels);

    default <T> T findAll(LabelName name, Collector<String, ?, T> collector) {
        return getLabels().stream()
                .filter(label -> name.value().equals(label.getName()))
                .map(Label::getValue)
                .collect(collector);
    }

    default List<String> findAll(LabelName name) {
        return findAll(name, Collectors.toList());
    }

    default Optional<String> findOne(LabelName name) {
        return getLabels().stream()
                .filter(label -> name.value().equals(label.getName()))
                .map(Label::getValue)
                .findAny();
    }
}
