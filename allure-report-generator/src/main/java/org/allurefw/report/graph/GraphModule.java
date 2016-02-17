package org.allurefw.report.graph;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
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
        bind(GraphData.class).in(Scopes.SINGLETON);

        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(GraphPlugin.class);


        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(GraphDataProvider.class);
    }
}
