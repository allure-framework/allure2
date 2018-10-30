package io.qameta.allure.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Jira launch export data.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraLaunch {

    private int id;

    private List<String> issueKeys;

    private String name;
    private String url;
    private Long date;

    private Long failed;
    private Long broken;
    private Long passed;
    private Long skipped;
    private Long unknown;

}
