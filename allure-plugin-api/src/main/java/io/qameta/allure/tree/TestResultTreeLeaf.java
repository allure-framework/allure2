package io.qameta.allure.tree;

import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestStatus;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeLeaf extends DefaultTreeLeaf {

    private final String uid;

    private final String parentUid;

    private final TestStatus status;

    private final Long start;

    private final Long stop;

    private final Long duration;

    private final boolean flaky;

    private final List<String> parameters;

    public TestResultTreeLeaf(final String parentUid, final TestResult testResult) {
        this(
                parentUid,
                testResult.getName(),
                testResult.getId(),
                testResult.getStatus(),
                testResult.getStart(),
                testResult.getStop(),
                testResult.getDuration(),
                testResult.isFlaky(),
                testResult.getParameterValues()
        );
    }

    public TestResultTreeLeaf(final String parentUid, final String name, final String uid,
                              final TestStatus status, final Long start, final Long stop, final Long duration,
                              final boolean flaky, final List<String> parameters) {
        super(name);
        this.parentUid = parentUid;
        this.uid = uid;
        this.status = status;
        this.start = start;
        this.stop = stop;
        this.duration = duration;
        this.flaky = flaky;
        this.parameters = parameters;
    }

    public String getParentUid() {
        return parentUid;
    }

    public String getUid() {
        return uid;
    }

    public TestStatus getStatus() {
        return status;
    }

    public Long getStart() {
        return start;
    }

    public Long getStop() {
        return stop;
    }

    public Long getDuration() {
        return duration;
    }

    public boolean isFlaky() {
        return flaky;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
