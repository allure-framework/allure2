package org.allurefw.report.timeline;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.TimelineData;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class TimelineModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(TimelinePlugin.class);

        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(TimelineDataProvider.class);
    }

    @Provides
    @Singleton
    protected TimelineData getData() {
        return new TimelineData();
    }

    public static class TimelineDataProvider implements ReportDataProvider {

        protected final TimelineData data;

        @Inject
        protected TimelineDataProvider(TimelineData data) {
            this.data = data;
        }

        @Override
        public Object provide() {
            return data;
        }

        @Override
        public String getFileName() {
            return "timeline.json";
        }
    }
}
