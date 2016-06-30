package org.allurefw.report.tree;

import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeGroup {

    List<String> getGroupNames();

    static TreeGroup values(String... values) {
        return () -> Arrays.asList(values);
    }

    static TreeGroup values(List<String> values) {
        return () -> Collections.unmodifiableList(values);
    }

    static TreeGroup allByLabel(TestCaseResult result, LabelName labelName, String... defaultGroups) {
        Set<String> groups = result.findAll(labelName, Collectors.toSet());
        if (groups.isEmpty()) {
            return values(defaultGroups);
        }
        return values(new ArrayList<>(groups));
    }

    static TreeGroup oneByLabel(TestCaseResult result, LabelName labelName, String defaultGroup) {
        return values(result.findOne(labelName).orElse(defaultGroup));
    }

}
