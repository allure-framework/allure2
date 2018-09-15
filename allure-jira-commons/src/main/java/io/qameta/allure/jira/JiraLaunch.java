package io.qameta.allure.jira;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Jira Launch Export Data.
 */
@Data
@Accessors(chain = true)
public class JiraLaunch {

    private int id;

    private String issueKey;

    private String name;
    private String url;

    private Long failed;
    private Long broken;
    private Long passed;
    private Long skipped;
    private Long unknown;

    private Long date;

}
