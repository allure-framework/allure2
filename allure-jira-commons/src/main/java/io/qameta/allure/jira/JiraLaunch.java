package io.qameta.allure.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Jira launch export data.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
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
