package io.qameta.allure.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class Group {

    protected String name;
    protected List<String> fields;
    protected Long order;

}
