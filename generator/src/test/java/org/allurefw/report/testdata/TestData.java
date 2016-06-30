package org.allurefw.report.testdata;

import org.allurefw.report.entity.TestGroup;
import org.allurefw.report.entity.TestCaseResult;

/**
 * @author charlie (Dmitry Baev).
 */
public final class TestData {

    TestData() {
    }

    public static TestCaseResult randomTestCase() {
        return new TestCaseResult().withName("some test case");
    }

    public static TestGroup randomTestGroup() {
        return new TestGroup()
                .withName("some group name");
    }
}
