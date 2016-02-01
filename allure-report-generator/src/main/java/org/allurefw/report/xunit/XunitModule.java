package org.allurefw.report.xunit;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.XunitData;

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
                .addBinding().to(XunitDataProvider.class);
    }

    @Provides
    @Singleton
    protected XunitData getData() {
        return new XunitData();
    }

    public static class XunitDataProvider implements ReportDataProvider {

        protected final XunitData data;

        @Inject
        protected XunitDataProvider(XunitData data) {
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
}
