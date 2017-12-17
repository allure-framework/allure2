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
public class TestResultExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<AttachmentLink> attachments = new ArrayList<>();
    protected List<TestResultStep> steps = new ArrayList<>();

}

