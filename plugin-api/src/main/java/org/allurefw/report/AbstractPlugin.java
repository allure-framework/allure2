package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import java.util.UUID;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 26.02.16
 */
public abstract class AbstractPlugin extends AbstractModule {

    public <T> AggregatorBuilder<T> aggregateResults(Class<? extends ResultAggregator<T>> aggregatorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, ResultAggregator.class)
                .addBinding(uid).to(aggregatorClass);

        return new AggregatorBuilder<>(uid);
    }

    public <T> AggregatorBuilder<T> aggregateTestCases(Class<? extends TestCaseAggregator<T>> aggregatorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, TestCaseAggregator.class)
                .addBinding(uid).to(aggregatorClass);
        return new AggregatorBuilder<>(uid);
    }

    public <T> AggregatorBuilder<T> aggregateTestRuns(Class<? extends TestRunAggregator<T>> aggregatorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, TestRunAggregator.class)
                .addBinding(uid).to(aggregatorClass);
        return new AggregatorBuilder<>(uid);
    }

    public void processor(Class<? extends Processor> processorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, Processor.class)
                .addBinding(uid).to(processorClass);
    }

    public class AggregatorBuilder<T> {

        private String uid;

        public AggregatorBuilder(String uid) {
            this.uid = uid;
        }

        public AggregatorBuilder<T> toReportData(String fileName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, DataFileNames.class)
                    .addBinding(uid).toInstance(fileName);
            return this;
        }

        public AggregatorBuilder<T> toReportData(String fileName, Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, Finalizer.class)
                    .addBinding(fileName).to(finalizerClass);
            return toReportData(fileName);
        }

        public AggregatorBuilder<T> toWidget(String widgetName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, WidgetNames.class)
                    .addBinding(uid).toInstance(widgetName);
            return this;
        }

        public AggregatorBuilder<T> toWidget(String widgetName, Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, Finalizer.class)
                    .addBinding(widgetName).to(finalizerClass);
            return toWidget(widgetName);
        }
    }
}
