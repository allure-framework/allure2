package io.qameta.allure.idea;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.TestResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IdeaLinksPluginTest {

    private static final String TEST_CLASS = "io.qameta.allure.AllureTest";

    @Rule
    public final EnvironmentVariables jiraEnabled = new EnvironmentVariables()
            .set("ALLURE_IDEA_ENABLED", "true");

    @Test
    public void shouldExportTestResultToJira() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult testResult = new TestResult()
                .setLabels(Collections.singletonList(new Label().setName("testClass").setValue(TEST_CLASS)));

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final IdeaLinksPlugin jiraTestResultExportPlugin = new IdeaLinksPlugin();

        jiraTestResultExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );

        assertThat(testResult.getLinks()).hasSize(1);
        final Link link = testResult.getLinks().get(0);
        assertThat(link.getName()).isEqualTo("Open in Idea");
        assertThat(link.getType()).isEqualTo("idea");
        assertThat(link.getUrl()).contains(TEST_CLASS.replace(".", "/"));

    }

}
