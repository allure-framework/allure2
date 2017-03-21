package io.qameta.allure;

import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportFactory {

    private final Set<ResultsReader> testCaseReaders;

    private final Set<Plugin> plugins;

    private final Map<String, TestCase> testCases;

    @Inject
    public ReportFactory(final Set<Plugin> plugins, final Set<ResultsReader> testCaseReaders) {
        this.plugins = plugins;
        this.testCaseReaders = testCaseReaders;
        this.testCases = new HashMap<>();
    }

    public ReportInfo create(final Path... sources) {
        final List<TestCaseResult> results = new ArrayList<>();
        for (Path source : sources) {
            final List<TestCaseResult> testCaseResults = readTestCases(source)
                    .collect(Collectors.toList());
            for (TestCaseResult result : testCaseResults) {
                TestCase testCase = getTestCase(result);
                testCase.updateLinks(result.getLinks());
                testCase.updateParametersNames(result.getParameters());
                testCase.getResults().add(result.toInfo());
            }
            results.addAll(testCaseResults);
        }
        return new ReportInfo(plugins, testCases, results);
    }

    private TestCase getTestCase(final TestCaseResult result) {
        return testCases.computeIfAbsent(result.getTestCaseId(), id -> new TestCase()
                .withId(id)
                .withName(result.getName())
                .withDescription(result.getDescription())
                .withDescriptionHtml(result.getDescriptionHtml())
        );
    }

    private Stream<TestCaseResult> readTestCases(final Path source) {
        return testCaseReaders.stream()
                .flatMap(reader -> reader.readResults(source).stream());
    }
}
