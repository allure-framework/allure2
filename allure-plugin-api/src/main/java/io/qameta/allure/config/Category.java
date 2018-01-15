package io.qameta.allure.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class Category {

    protected String name;
    protected String matchedRegex;
    protected List<String> matchedStatuses;

}
