package org.allurefw.report.behaviors;

import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.tree.TreeAggregator;
import org.allurefw.report.tree.TreeGroup;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class BehaviorsAggregator extends TreeAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return Arrays.asList(
                TreeGroup.allByLabel(result, LabelName.FEATURE, "Default feature"),
                TreeGroup.allByLabel(result, LabelName.STORY, "Default story")
        );
    }
}
