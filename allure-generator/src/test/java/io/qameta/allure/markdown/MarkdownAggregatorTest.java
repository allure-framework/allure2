package io.qameta.allure.markdown;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.MarkdownDescriptionsPlugin;
import io.qameta.allure.entity.TestCaseResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownAggregatorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final Configuration configuration = new ConfigurationBuilder().useDefault().build();

    @Test
    public void shouldNotFailIfEmptyResults() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();
        aggregator.aggregate(configuration, Collections.emptyList(), output);
    }

    @Test
    public void shouldSkipResultsWithEmptyDescription() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestCaseResult result = new TestCaseResult().withName("some");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .hasFieldOrPropertyWithValue("description", null)
                .hasFieldOrPropertyWithValue("descriptionHtml", null);
    }

    @Test
    public void shouldSkipResultsWithNonEmptyDescriptionHtml() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestCaseResult result = new TestCaseResult()
                .withName("some")
                .withDescription("desc")
                .withDescriptionHtml("descHtml");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .hasFieldOrPropertyWithValue("description", "desc")
                .hasFieldOrPropertyWithValue("descriptionHtml", "descHtml");
    }

    @Test
    public void shouldProcessDescription() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestCaseResult result = new TestCaseResult()
                .withName("some")
                .withDescription("desc");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .hasFieldOrPropertyWithValue("description", "desc")
                .hasFieldOrPropertyWithValue("descriptionHtml", "<p>desc</p>");
    }

}