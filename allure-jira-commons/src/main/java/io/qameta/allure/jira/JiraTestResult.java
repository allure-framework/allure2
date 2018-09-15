package io.qameta.allure.jira;

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

    private String status;
    private Long date;

}
