package org.allurefw.report.packages;

import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.tree.TreeResultAggregator;
import org.allurefw.report.tree.TreeGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class PackagesResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return result.findOne(LabelName.TEST_CLASS)
                .map(testClass -> Arrays.asList(testClass.split("\\.")))
                .orElse(Collections.singletonList("Without package"))
                .stream()
                .map(TreeGroup::values)
                .collect(Collectors.toList());
    }
}
