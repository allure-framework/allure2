package io.qameta.allure.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Jira test result export data.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
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
