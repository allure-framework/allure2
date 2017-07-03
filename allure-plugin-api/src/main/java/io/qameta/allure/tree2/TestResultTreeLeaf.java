package io.qameta.allure.tree2;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;

import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeLeaf extends DefaultTreeLeaf {

    private final String uid;

    private final Status status;

    private final Time time;

    private final boolean flaky;

    private final List<String> parameters;

    public TestResultTreeLeaf(final TestResult testResult) {
        this(
                testResult.getName(),
                testResult.getUid(),
                testResult.getStatus(),
                testResult.getTime(),
                testResult.getStatusDetailsSafe().isFlaky(),
                testResult.getParameterValues()
        );
    }

    public TestResultTreeLeaf(final String name, final String uid,
                              final Status status, final Time time,
                              final boolean flaky, final List<String> parameters) {
        super(name);
        this.uid = uid;
        this.status = status;
        this.time = time;
        this.flaky = flaky;
        this.parameters = parameters;
    }

    public String getUid() {
        return uid;
    }

    public Status getStatus() {
        return status;
    }

    public Time getTime() {
        return time;
    }

    public boolean isFlaky() {
        return flaky;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public static Optional<TestResultTreeLeaf> create(final TestResult testResult) {
        return Optional.of(new TestResultTreeLeaf(testResult));
    }
}
