package io.qameta.allure;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

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
            toData("report-data-folder", fileName);
            return this;
        }

        public AggregatorBuilder<T> toReportData(String fileName, Class<? extends Finalizer<T>> finalizerClass) {
            toData("report-data-folder", fileName, finalizerClass);
            return this;
        }

        public AggregatorBuilder<T> toWidget(String widgetName) {
            toData("report-widgets", widgetName);
            return this;
        }

        public AggregatorBuilder<T> toWidget(String widgetName, Class<? extends Finalizer<T>> finalizerClass) {
            toData("report-widgets", widgetName, finalizerClass);
            return this;
        }

        private AggregatorBuilder<T> toData(String dataName, String fileName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, Names.named(dataName))
                    .addBinding(uid).toInstance(fileName);
            return this;
        }

        private AggregatorBuilder<T> toData(String dataName, String widgetName, Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, Finalizer.class)
                    .addBinding(widgetName).to(finalizerClass);
            return toData(dataName, widgetName);
        }
    }
}
