package io.qameta.allure.tree;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class Layer {

    private final List<GroupNodeContext> groups;

    public Layer(final String key, final Collection<String> values) {
        this(values.stream()
                .map(value -> new GroupNodeContext(key, value))
                .collect(Collectors.toList())
        );
    }

    public Layer(final List<GroupNodeContext> groups) {
        this.groups = Collections.unmodifiableList(groups);
    }

}
