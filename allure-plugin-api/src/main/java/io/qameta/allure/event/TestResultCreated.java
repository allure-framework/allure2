package io.qameta.allure.event;

import io.qameta.allure.entity.TestResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class TestResultCreated implements Serializable {

    private static final long serialVersionUID = 1L;

    protected TestResult body;

    public TestResultCreated(final TestResult body) {
        this.body = body;
    }

}
