package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class Link implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected String url;
    protected String type;

}
