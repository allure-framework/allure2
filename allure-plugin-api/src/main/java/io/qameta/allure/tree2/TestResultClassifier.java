package io.qameta.allure.tree2;

import io.qameta.allure.entity.TestResult;

import java.util.Collections;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultClassifier implements Classifier<TestResult> {

    private final List<String> groups;

    public TestResultClassifier(final String group) {
        this(Collections.singletonList(group));
    }

    public TestResultClassifier(final List<String> groups) {
        this.groups = groups;
    }

    @Override
    public List<String> classify(final TestResult item) {
        return groups;
    }

    @Override
    public TreeGroup factory(final String name, final TestResult item) {
        return new TestResultTreeGroup(name, name);
    }
}
