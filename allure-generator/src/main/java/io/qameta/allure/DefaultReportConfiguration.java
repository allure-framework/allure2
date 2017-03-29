package io.qameta.allure;

import io.qameta.allure.allure1.Allure1ResultsReader;
import io.qameta.allure.allure2.Allure2ResultsReader;
import io.qameta.allure.category.CategoryPlugin;
import io.qameta.allure.core.AttachmentsPlugin;
import io.qameta.allure.core.IndexHtmlPlugin;
import io.qameta.allure.core.ResultsPlugin;
import io.qameta.allure.executor.ExecutorReader;
import io.qameta.allure.freemarker.FreemarkerContext;
import io.qameta.allure.graph.GraphPlugin;
import io.qameta.allure.history.HistoryPlugin;
import io.qameta.allure.mail.MailPlugin;
import io.qameta.allure.markdown.MarkdownContext;
import io.qameta.allure.markdown.MarkdownProcessor;
import io.qameta.allure.owner.OwnerProcessor;
import io.qameta.allure.severity.SeverityProcessor;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.timeline.TimelinePlugin;
import io.qameta.allure.widget.WidgetPlugin;
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
public class DefaultReportConfiguration implements ReportConfiguration {

    private final Map<Class, Object> context = new HashMap<>();

    public DefaultReportConfiguration() {
        context.put(JacksonMapperContext.class, new JacksonMapperContext());
        context.put(MarkdownContext.class, new MarkdownContext());
        context.put(FreemarkerContext.class, new FreemarkerContext());
    }

    @Override
    public List<Plugin> getPlugins() {
        return Collections.emptyList();
    }

    @Override
    public List<Processor> getProcessors() {
        return Arrays.asList(
                new MarkdownProcessor(),
                new SeverityProcessor(),
                new OwnerProcessor(),
                new CategoryPlugin(),
                new HistoryPlugin()
        );
    }

    @Override
    public List<Aggregator> getAggregators() {
        return Arrays.asList(
                new CategoryPlugin(),
                new HistoryPlugin(),
                new GraphPlugin(),
                new TimelinePlugin(),
                new XunitPlugin(),
                new IndexHtmlPlugin(),
                new ResultsPlugin(),
                new AttachmentsPlugin(),
                new MailPlugin(),
                new WidgetPlugin(),
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
    public List<WidgetAggregator> getWidgetAggregators() {
        return Collections.singletonList(
                new SummaryPlugin()
        );
    }

    @Override
    public <T> Optional<T> getContext(Class<T> contextType) {
        return Optional.ofNullable(contextType.cast(context.get(contextType)));
    }
}
