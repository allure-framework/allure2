package org.allurefw.report.xunit;

import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.tree.TreeResultAggregator;
import org.allurefw.report.tree.TreeGroup;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 05.03.16
 */
public class XunitResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return Collections.singletonList(
                TreeGroup.oneByLabel(result, LabelName.SUITE, "Default suite")
        );
    }
}
