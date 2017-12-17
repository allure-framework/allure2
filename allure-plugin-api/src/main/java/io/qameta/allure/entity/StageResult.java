package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class StageResult implements Serializable, Summarizable {

    private static final long serialVersionUID = 1L;

    protected String name;

    protected Long start;
    protected Long stop;
    protected Long duration;

    protected TestStatus status;
    protected String message;
    protected String trace;

    protected List<TestResultStep> steps = new ArrayList<>();
    protected List<Attachment> attachments = new ArrayList<>();
    protected List<TestParameter> parameters = new ArrayList<>();

}
