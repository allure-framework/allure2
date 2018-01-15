package io.qameta.allure.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class ReportConfig {

    protected String version;
    protected String reportName;

    protected List<String> environmentVariables;
    protected List<String> customFields;
    protected List<String> tags;

    protected Map<String, Group> groups;
    protected List<Category> categories;

}
