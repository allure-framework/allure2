package org.allurefw.report.graph;

import com.google.inject.Inject;
import org.allurefw.report.GraphData;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.entity.TestCase;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class GraphPlugin implements TestCaseProcessor {

    @Inject
    protected GraphData data;

    @Override
    public void process(TestCase testCase) {
        data.getTestCases().add(testCase.toInfo());
    }
}
