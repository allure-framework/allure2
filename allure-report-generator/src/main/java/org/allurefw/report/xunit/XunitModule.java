package org.allurefw.report.xunit;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.WidgetDataProvider;
import org.allurefw.report.XunitData;
import org.allurefw.report.XunitWidgetData;

import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class XunitModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(XunitPlugin.class);

        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(XunitReportDataProvider.class);

        Multibinder.newSetBinder(binder(), WidgetDataProvider.class)
                .addBinding().to(XunitWidgetDataProvider.class);
    }

    @Provides
    @Singleton
    protected XunitData getData() {
        return new XunitData();
    }

    public static class XunitReportDataProvider implements ReportDataProvider {

        protected final XunitData data;

        @Inject
        protected XunitReportDataProvider(XunitData data) {
            this.data = data;
        }

        @Override
        public Object provide() {
            return data;
        }

        @Override
        public String getFileName() {
            return "xunit.json";
        }
    }

    public static class XunitWidgetDataProvider implements WidgetDataProvider {

        protected final XunitData data;

        @Inject
        protected XunitWidgetDataProvider(XunitData data) {
            this.data = data;
        }

        @Override
        public Object provide() {
            return data.getTestSuites().stream()
                    .sorted()
                    .limit(10)
                    .map(testSuite -> new XunitWidgetData()
                            .withUid(testSuite.getUid())
                            .withName(testSuite.getName()))
                    .collect(Collectors.toList());
        }

        @Override
        public String getWidgetId() {
            return "xunit";
        }
    }
}
