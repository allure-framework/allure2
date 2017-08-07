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

    private final List<String> parameters;

    public TestResultTreeLeaf(final String parentUid, final TestResult testResult) {
        this(
                parentUid,
                testResult.getName(),
                testResult.getUid(),
                testResult.getStatus(),
                testResult.getTime(),
                testResult.getStatusDetailsSafe().isFlaky(),
                testResult.getParameterValues()
        );
    }

    public TestResultTreeLeaf(final String parentUid, final String name, final String uid,
                              final Status status, final Time time,
                              final boolean flaky, final List<String> parameters) {
        super(name);
        this.parentUid = parentUid;
        this.uid = uid;
        this.status = status;
        this.time = time;
        this.flaky = flaky;
        this.parameters = parameters;
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

    public List<String> getParameters() {
        return parameters;
    }
}
