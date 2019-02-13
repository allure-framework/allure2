package io.qameta.allure.tree;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeLeaf extends DefaultTreeLeaf {

    private final String uid;

    private final String parentUid;

    private final Status status;

    private final Time time;

    private final boolean flaky;

    private final boolean newFailed;

    private final List<String> parameters;

    public TestResultTreeLeaf(final String parentUid, final TestResult testResult) {
        this(
                parentUid,
                testResult.getName(),
                testResult
        );
    }

    public TestResultTreeLeaf(final String parentUid, final String name, final TestResult testResult) {
        super(name);
        this.parentUid = parentUid;
        this.uid = testResult.getUid();
        this.status = testResult.getStatus();
        this.time = testResult.getTime();
        this.flaky = testResult.isFlaky();
        this.newFailed = testResult.isNewFailed();
        this.parameters = testResult.getParameterValues();

    }
    public String getParentUid() {
        return parentUid;
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

    public boolean isNewFailed() {
        return newFailed;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
