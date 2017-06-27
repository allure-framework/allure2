package io.qameta.allure.tree2;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public final class TreeUtils {

    private TreeUtils() {
        throw new IllegalStateException("Do not instance");
    }

    public static List<Classifier<TestResult>> groupByLabels(final Supplier<String> uidGenerator,
                                                             final TestResult testResult,
                                                             final LabelName... labelNames) {
        return Stream.of(labelNames)
                .map(testResult::findAll)
                .filter(strings -> !strings.isEmpty())
                .map(names -> new Classifier<TestResult>() {
                    @Override
                    public List<String> classify(final TestResult item) {
                        return names;
                    }

                    @Override
                    public TreeGroup factory(final String name, final TestResult item) {
                        return new TestResultTreeGroup(name, uidGenerator.get());
                    }
                })
                .collect(Collectors.toList());
    }
}
