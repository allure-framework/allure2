package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class Tree implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected List<CustomField> fields;

}
