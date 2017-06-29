package io.qameta.allure;

import io.qameta.allure.allure1.Allure1Plugin;
import io.qameta.allure.allure2.Allure2Plugin;
import io.qameta.allure.category.CategoriesPlugin;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.AttachmentsPlugin;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.MarkdownDescriptionsPlugin;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.core.ReportWebPlugin;
import io.qameta.allure.core.TestsResultsPlugin;
import io.qameta.allure.environment.Allure1EnvironmentPlugin;
import io.qameta.allure.executor.ExecutorPlugin;
import io.qameta.allure.graph.GraphPlugin;
import io.qameta.allure.history.HistoryPlugin;
import io.qameta.allure.history.HistoryTrendPlugin;
import io.qameta.allure.launch.LaunchPlugin;
import io.qameta.allure.mail.MailPlugin;
import io.qameta.allure.owner.OwnerPlugin;
import io.qameta.allure.retry.RetryPlugin;
import io.qameta.allure.severity.SeverityPlugin;
import io.qameta.allure.suites.SuitesPlugin;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.tags.TagsPlugin;
import io.qameta.allure.timeline.TimelinePlugin;
import io.qameta.allure.widget.WidgetsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builder for {@link Configuration}.
 *
 * @see Configuration
 * @since 2.0
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class ConfigurationBuilder {

    private final List<Extension> extensions = new ArrayList<>();

    private final List<Plugin> plugins = new ArrayList<>();

    public ConfigurationBuilder useDefault() {
        fromExtensions(Arrays.asList(
                new JacksonContext(),
                new MarkdownContext(),
                new FreemarkerContext(),
                new RandomUidContext(),
                new MarkdownDescriptionsPlugin(),
                new RetryPlugin(),
                new TagsPlugin(),
                new SeverityPlugin(),
                new OwnerPlugin(),
                new CategoriesPlugin(),
                new HistoryPlugin(),
                new HistoryTrendPlugin(),
                new GraphPlugin(),
                new TimelinePlugin(),
                new SuitesPlugin(),
                new ReportWebPlugin(),
                new TestsResultsPlugin(),
                new AttachmentsPlugin(),
                new MailPlugin(),
                new WidgetsPlugin(),
                new SummaryPlugin(),
                new ExecutorPlugin(),
                new LaunchPlugin(),
                new Allure1Plugin(),
                new Allure1EnvironmentPlugin(),
                new Allure2Plugin()
        ));
        return this;
    }

    public ConfigurationBuilder fromExtensions(final List<Extension> extensions) {
        this.extensions.addAll(extensions);
        return this;
    }

    public ConfigurationBuilder fromPlugins(final List<Plugin> plugins) {
        this.plugins.addAll(plugins);
        plugins.stream()
                .map(Plugin::getExtensions)
                .forEach(this::fromExtensions);
        return this;
    }

    public Configuration build() {
        return new DefaultConfiguration(
                Collections.unmodifiableList(extensions),
                Collections.unmodifiableList(plugins)
        );
    }
}
