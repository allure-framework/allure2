package io.qameta.allure.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeLayer implements TreeLayer {

    private final List<String> groupNames;

    public DefaultTreeLayer(final String... groupNames) {
        this(Arrays.asList(groupNames));
    }

    public DefaultTreeLayer(final Collection<String> groupNames) {
        this.groupNames = new ArrayList<>(groupNames);
    }

    @Override
    public List<String> getGroupNames() {
        return Collections.unmodifiableList(groupNames);
    }
}
