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

    public <T> AggregatorBuilder<T> aggregateResults(final Class<? extends ResultAggregator<T>> aggregatorClass) {
        final String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, ResultAggregator.class)
                .addBinding(uid).to(aggregatorClass);

        return new AggregatorBuilder<>(uid);
    }

    public <T> AggregatorBuilder<T> aggregateTestCases(final Class<? extends TestCaseAggregator<T>> aggregatorClass) {
        final String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, TestCaseAggregator.class)
                .addBinding(uid).to(aggregatorClass);
        return new AggregatorBuilder<>(uid);
    }

    public <T> AggregatorBuilder<T> aggregateTestRuns(final Class<? extends TestRunAggregator<T>> aggregatorClass) {
        final String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, TestRunAggregator.class)
                .addBinding(uid).to(aggregatorClass);
        return new AggregatorBuilder<>(uid);
    }

    public void processor(final Class<? extends Processor> processorClass) {
        final String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, Processor.class)
                .addBinding(uid).to(processorClass);
    }

    public class AggregatorBuilder<T> {

        private final String uid;

        public AggregatorBuilder(final String uid) {
            this.uid = uid;
        }

        public AggregatorBuilder<T> toReportData(final String fileName) {
            toData("report-data-folder", fileName);
            return this;
        }

        public AggregatorBuilder<T> toReportData(final String fileName,
                                                 final Class<? extends Finalizer<T>> finalizerClass) {
            toData("report-data-folder", fileName, finalizerClass);
            return this;
        }

        public AggregatorBuilder<T> toWidget(final String widgetName) {
            toData("report-widgets", widgetName);
            return this;
        }

        public AggregatorBuilder<T> toWidget(final String widgetName,
                                             final Class<? extends Finalizer<T>> finalizerClass) {
            toData("report-widgets", widgetName, finalizerClass);
            return this;
        }

        private AggregatorBuilder<T> toData(final String dataName,
                                            final String fileName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, Names.named(dataName))
                    .addBinding(uid).toInstance(fileName);
            return this;
        }

        private AggregatorBuilder<T> toData(final String dataName,
                                            final String widgetName,
                                            final Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, Finalizer.class)
                    .addBinding(widgetName).to(finalizerClass);
            return toData(dataName, widgetName);
        }
    }
}
