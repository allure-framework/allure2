package io.qameta.allure.jira;

import io.qameta.allure.entity.Status;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Jira TestResult export data.
 */
@Data
@Accessors(chain = true)
public class JiraTestResult {

    private int id;

    private String issueKey;

    private String name;
    private String url;

    private String launchUrl;
    private String launchName;

    private Status status;
    private Long date;

}
