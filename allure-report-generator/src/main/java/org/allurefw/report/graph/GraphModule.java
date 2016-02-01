package org.allurefw.report.graph;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.GraphData;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class GraphModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(GraphPlugin.class);


        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(GraphDataProvider.class);
    }

    @Provides
    @Singleton
    protected GraphData getData() {
        return new GraphData();
    }

    public static class GraphDataProvider implements ReportDataProvider {

        protected final GraphData data;

        @Inject
        protected GraphDataProvider(GraphData data) {
            this.data = data;
        }

        @Override
        public Object provide() {
            return data;
        }

        @Override
        public String getFileName() {
            return "graph.json";
        }
    }
}
