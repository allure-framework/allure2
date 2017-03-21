package io.qameta.allure.core;

import io.qameta.allure.TestRunAggregator;
import io.qameta.allure.TestRunDetailsReader;
import io.qameta.allure.TestRunReader;
import io.qameta.allure.entity.TestRun;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessTestRunStage {

    protected final TestRunReader reader;

    protected final Set<TestRunDetailsReader> detailsReaders;

    private final Map<String, TestRunAggregator> aggregators;

    @Inject
    public ProcessTestRunStage(final TestRunReader reader,
                               final Set<TestRunDetailsReader> detailsReaders,
                               final Map<String, TestRunAggregator> aggregators) {
        this.reader = reader;
        this.detailsReaders = detailsReaders;
        this.aggregators = aggregators;
    }

    @SuppressWarnings("unchecked")
    public Consumer<Map<String, Object>> process(final TestRun testRun) {
        return data -> aggregators.forEach((uid, aggregator) -> {
            final Object value = data.computeIfAbsent(uid, key -> aggregator.supplier().get());
            aggregator.aggregate(testRun).accept(value);
        });
    }

    public Function<Path, TestRun> read() {
        return source -> {
            final TestRun testRun = reader.readTestRun(source);
            detailsReaders.forEach(reader -> reader.readDetails(source).accept(testRun));
            return testRun;
        };
    }
}
