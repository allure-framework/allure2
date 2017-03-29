package io.qameta.allure;

import io.qameta.allure.allure1.Allure1ResultsReader;
import io.qameta.allure.allure2.Allure2ResultsReader;
import io.qameta.allure.category.CategoryPlugin;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.AttachmentsPlugin;
import io.qameta.allure.core.IndexHtmlPlugin;
import io.qameta.allure.core.ResultsPlugin;
import io.qameta.allure.executor.ExecutorReader;
import io.qameta.allure.graph.GraphPlugin;
import io.qameta.allure.history.HistoryPlugin;
import io.qameta.allure.mail.MailPlugin;
import io.qameta.allure.markdown.MarkdownPlugin;
import io.qameta.allure.owner.OwnerPlugin;
import io.qameta.allure.severity.SeverityPlugin;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.timeline.TimelinePlugin;
import io.qameta.allure.widget.WidgetsPlugin;
import io.qameta.allure.xunit.XunitPlugin;

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
    public List<Plugin> getPlugins() {
        return Arrays.asList(
                new MarkdownPlugin(),
                new SeverityPlugin(),
                new OwnerPlugin(),
                new CategoryPlugin(),
                new HistoryPlugin(),
                new GraphPlugin(),
                new TimelinePlugin(),
                new XunitPlugin(),
                new IndexHtmlPlugin(),
                new ResultsPlugin(),
                new AttachmentsPlugin(),
                new MailPlugin(),
                new WidgetsPlugin(),
                new SummaryPlugin()
        );
    }

    @Override
    public List<ResultsReader> getReaders() {
        return Arrays.asList(
                new Allure1ResultsReader(),
                new Allure2ResultsReader(),
                new CategoryPlugin(),
                new HistoryPlugin(),
                new ExecutorReader()
        );
    }

    @Override
    public List<WidgetPlugin> getWidgetPlugins() {
        return Collections.singletonList(
                new SummaryPlugin()
        );
    }

    @Override
    public <T> Optional<T> getContext(final Class<T> contextType) {
        return Optional.ofNullable(contextType.cast(context.get(contextType)));
    }
}
