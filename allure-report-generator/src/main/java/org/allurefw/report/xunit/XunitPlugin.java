package org.allurefw.report.xunit;

import com.google.inject.Inject;
import org.allurefw.LabelName;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.TestSuite;
import org.allurefw.report.XunitData;
import org.allurefw.report.entity.TestCase;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class XunitPlugin implements TestCaseProcessor {

    @Inject
    protected XunitData data;

    @Override
    public void process(TestCase testCase) {
        String suiteName = testCase.findOne(LabelName.SUITE)
                .orElse("Default suite");

        TestSuite testSuite = data.getTestSuites().stream()
                .filter(item -> suiteName.equals(item.getName()))
                .findAny()
                .orElseGet(() -> {
                    TestSuite newOne = new TestSuite().withName(suiteName).withUid(generateUid());
                    data.getTestSuites().add(newOne);
                    return newOne;
                });

        testSuite.getTestCases().add(testCase.toInfo());
    }
}
