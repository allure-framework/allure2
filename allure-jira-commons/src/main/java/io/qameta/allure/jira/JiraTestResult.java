package io.qameta.allure.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Jira test result export data.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraTestResult {

    private int id;

    private Integer launchId;
    private List<String> issueKeys;

    private String name;
    private String url;
    private Long date;

    private String status;

}
