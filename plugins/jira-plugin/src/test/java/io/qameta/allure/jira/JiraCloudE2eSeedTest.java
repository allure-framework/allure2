package io.qameta.allure.jira;

import io.qameta.allure.Issue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class JiraCloudE2eSeedTest {

    @Test
    @Issue("AIL-3")
    void shouldCreateIssueLinkForCloudExport() {
        fail("force failure to test Jira Cloud export");
    }
}
