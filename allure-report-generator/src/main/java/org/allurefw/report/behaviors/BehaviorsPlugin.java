package org.allurefw.report.behaviors;

import org.allurefw.LabelName;
import org.allurefw.report.BehaviorData;
import org.allurefw.report.TestCase;
import org.allurefw.report.TestCaseProcessor;

import java.util.List;

import static org.allurefw.ModelUtils.getLabels;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class BehaviorsPlugin implements TestCaseProcessor {

    private BehaviorData data = new BehaviorData();

    @Override
    public void process(TestCase testCase) {
        List<String> features = getLabels(testCase.getLabels(), LabelName.FEATURE);
    }
}
