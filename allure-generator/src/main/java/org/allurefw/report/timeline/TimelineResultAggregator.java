package org.allurefw.report.timeline;

import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.tree.TreeResultAggregator;
import org.allurefw.report.tree.TreeGroup;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 17.04.16
 */
public class TimelineResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return Arrays.asList(
                TreeGroup.oneByLabel(result, LabelName.HOST, "Default hostname"),
                TreeGroup.oneByLabel(result, LabelName.THREAD, "Default thread")
        );
    }
}
