package org.allurefw.report.widgets;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.WidgetDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class WidgetsModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(WidgetsDataProvider.class);
    }

    @Provides
    @Singleton
    protected WidgetsData getData(Set<WidgetDataProvider> widgets) {
        return new WidgetsData(widgets.stream().collect(
                HashMap::new,
                (map, provider) -> map.put(provider.getWidgetId(), provider.provide()),
                HashMap::putAll
        ));
    }


    public static class WidgetsData extends HashMap<String, Object> {
        public WidgetsData(Map<? extends String, ?> m) {
            super(m);
        }
    }

    public static class WidgetsDataProvider implements ReportDataProvider {

        protected final WidgetsData data;

        @Inject
        protected WidgetsDataProvider(WidgetsData data) {
            this.data = data;
        }

        @Override
        public Object provide() {
            return data;
        }

        @Override
        public String getFileName() {
            return "widgets.json";
        }
    }
}
