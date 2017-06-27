package io.qameta.allure.tree2;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;

import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeLeaf extends DefaultTreeLeaf {

    private final String uid;

    private final Status status;

    private final Time time;

    private final boolean isFlaky;

    public TestResultTreeLeaf(final TestResult testResult) {
        super(testResult.getName());
        this.uid = testResult.getUid();
        this.status = testResult.getStatus();
        this.time = testResult.getTime();
        this.isFlaky = testResult.getStatusDetailsSafe().isFlaky();
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
        return isFlaky;
    }

    public static Optional<TestResultTreeLeaf> create(final TestResult testResult) {
        return Optional.of(new TestResultTreeLeaf(testResult));
    }
}
