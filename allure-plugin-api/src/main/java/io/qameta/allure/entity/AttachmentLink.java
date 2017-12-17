package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class AttachmentLink implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long id;
    protected String name;
    protected String fileName;
    protected String contentType;
    protected Long contentLength;

}
