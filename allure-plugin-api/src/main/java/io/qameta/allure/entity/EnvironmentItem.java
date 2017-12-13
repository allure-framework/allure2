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
public class EnvironmentItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<String> values = new ArrayList<>();
    protected String name;

}
