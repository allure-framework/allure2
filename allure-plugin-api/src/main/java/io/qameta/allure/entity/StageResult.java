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
    protected Time time;

    protected String description;
    protected String descriptionHtml;

    protected Status status;
    protected String statusMessage;
    protected String statusTrace;

    protected List<Step> steps = new ArrayList<>();
    protected List<Attachment> attachments = new ArrayList<>();
    protected List<Parameter> parameters = new ArrayList<>();

}
