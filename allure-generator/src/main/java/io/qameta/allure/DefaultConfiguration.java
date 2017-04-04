package io.qameta.allure;

import io.qameta.allure.allure1.Allure1Reader;
import io.qameta.allure.allure2.Allure2Reader;
import io.qameta.allure.category.CategoryAggregator;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.AttachmentsAggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.PluginDescriptor;
import io.qameta.allure.core.ResultsAggregator;
import io.qameta.allure.core.StaticAggregator;
import io.qameta.allure.executor.ExecutorPlugin;
import io.qameta.allure.graph.GraphAggregator;
import io.qameta.allure.history.HistoryAggregator;
import io.qameta.allure.launch.LaunchPlugin;
import io.qameta.allure.mail.MailAggregator;
import io.qameta.allure.markdown.MarkdownAggregator;
import io.qameta.allure.owner.OwnerAggregator;
import io.qameta.allure.severity.SeverityAggregator;
import io.qameta.allure.summary.SummaryAggregator;
import io.qameta.allure.timeline.TimelineAggregator;
import io.qameta.allure.widget.WidgetsAggregator;
import io.qameta.allure.xunit.XunitAggregator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultConfiguration implements Configuration {

    private final Map<Class, Object> context = new HashMap<>();

    public DefaultConfiguration() {
        context.put(JacksonContext.class, new JacksonContext());
        context.put(MarkdownContext.class, new MarkdownContext());
        context.put(FreemarkerContext.class, new FreemarkerContext());
        context.put(RandomUidContext.class, new RandomUidContext());
    }

    @Override
    public List<PluginDescriptor> getPluginsDescriptors() {
        return Collections.emptyList();
    }

    @Override
    public List<Aggregator> getAggregators() {
        return Arrays.asList(
                new MarkdownAggregator(),
                new SeverityAggregator(),
                new OwnerAggregator(),
                new CategoryAggregator(),
                new HistoryAggregator(),
                new GraphAggregator(),
                new TimelineAggregator(),
                new XunitAggregator(),
                new StaticAggregator(),
                new ResultsAggregator(),
                new AttachmentsAggregator(),
                new MailAggregator(),
                new WidgetsAggregator(),
                new SummaryAggregator()
        );
    }

    @Override
    public List<Reader> getReaders() {
        return Arrays.asList(
                new Allure1Reader(),
                new Allure2Reader(),
                new CategoryAggregator(),
                new HistoryAggregator(),
                new ExecutorPlugin(),
                new LaunchPlugin()
        );
    }

    @Override
    public List<Widget> getWidgets() {
        return Arrays.asList(
                new SummaryAggregator(),
                new ExecutorPlugin(),
                new LaunchPlugin()
        );
    }

    @Override
    public <T> Optional<T> getContext(final Class<T> contextType) {
        return Optional.ofNullable(contextType.cast(context.get(contextType)));
    }
}
