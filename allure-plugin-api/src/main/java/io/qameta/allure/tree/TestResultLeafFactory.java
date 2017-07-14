package io.qameta.allure.tree;

import io.qameta.allure.entity.TestResult;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultLeafFactory implements TreeLeafFactory<TestResult, TestResultTreeLeaf> {

    @Override
    public TestResultTreeLeaf create(final TreeGroup parent, final TestResult item) {
        return new TestResultTreeLeaf(item);
    }

}
