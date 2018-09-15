package io.qameta.allure.markdown;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.MarkdownDescriptionsPlugin;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.TestResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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

        final TestResult result = new TestResult().setName("some");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("", "");
    }

    @Test
    public void shouldSkipResultsWithNonEmptyDescriptionHtml() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setName("some")
                .setDescription("desc")
                .setDescriptionHtml("descHtml");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("desc", "descHtml");
    }

    @Test
    public void shouldProcessDescription() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setName("some")
                .setDescription("desc");
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("desc", "<p>desc</p>\n");
    }

    @Test
    public void shouldConcatFixtureDescription() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setDescriptionHtml("Test description")
                .setBeforeStages(Collections.singletonList(new StageResult().setDescriptionHtml("Before descriptionHtml")));
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("", "Before descriptionHtml</br>Test description");
    }

    @Test
    public void shouldProcessFixturesDescriptionForNullTestDescription() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setBeforeStages(Arrays.asList(
                        new StageResult().setDescriptionHtml("Before1"),
                        new StageResult().setDescriptionHtml("Before2"),
                        new StageResult().setDescription("Before3")
                ));
        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("", "Before1</br>Before2");
    }

    @Test
    public void shouldNotProcessNullsInDescription() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setBeforeStages(Collections.singletonList(new StageResult().setDescriptionHtml("Before")));

        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescriptionHtml)
                .containsExactly("Before");
    }

    @Test
    public void shouldNotProcessNullsOfFixtureDescriptions() throws Exception {
        final Path output = folder.newFolder().toPath();
        final MarkdownDescriptionsPlugin aggregator = new MarkdownDescriptionsPlugin();

        final TestResult result = new TestResult()
                .setDescription("description")
                .setBeforeStages(Collections.singletonList(new StageResult()));

        final DefaultLaunchResults launchResults = new DefaultLaunchResults(
                Collections.singleton(result),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
        aggregator.aggregate(configuration, Collections.singletonList(launchResults), output);
        assertThat(result)
                .extracting(TestResult::getDescription, TestResult::getDescriptionHtml)
                .containsExactly("description", "<p>description</p>\n");
    }

}