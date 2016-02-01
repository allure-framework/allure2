package org.allurefw.report.defects;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.DefectsData;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class DefectsModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(DefectsPlugin.class);

        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(DefectsDataProvider.class);
    }

    @Provides
    @Singleton
    protected DefectsData getData() {
        return new DefectsData();
    }

    public static class DefectsDataProvider implements ReportDataProvider {

        protected final DefectsData data;

        @Inject
        protected DefectsDataProvider(DefectsData data) {
            this.data = data;
        }

        @Override
        public Object provide() {
            return data;
        }

        @Override
        public String getFileName() {
            return "defects.json";
        }
    }
}
