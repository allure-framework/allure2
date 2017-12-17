package io.qameta.allure.category;

import io.qameta.allure.entity.TestStatus;
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
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected String description;
    protected String descriptionHtml;
    protected String messageRegex;
    protected String traceRegex;
    protected List<TestStatus> matchedStatuses = new ArrayList<>();
    protected boolean flaky;

}
