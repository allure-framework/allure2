package org.allurefw.report;

import org.allurefw.report.entity.Link;
import org.allurefw.report.entity.Parameter;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;

import javax.inject.Inject;
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

    private final Set<TestCaseResultsReader> readers;

    private final Set<Plugin> plugins;

    @Inject
    public ReportFactory(Set<Plugin> plugins, Set<TestCaseResultsReader> readers) {
        this.plugins = plugins;
        this.readers = readers;
    }

    public Report create(ResultsSource... sources) {
        Map<String, TestCase> testCases = new HashMap<>();
        List<TestCaseResult> results = Stream.of(sources)
                .flatMap(this::readTestCases)
                .collect(Collectors.toList());

        results.forEach(result -> {
            TestCase testCase = testCases.computeIfAbsent(
                    result.getId(),
                    id -> createTestCase(result)
            );
            testCase.getResults().add(result.toInfo());

            List<String> parameterNames = Stream.concat(
                    testCase.getParametersNames().stream(),
                    result.getParameters().stream().map(Parameter::getName)
            ).distinct().collect(Collectors.toList());
            testCase.setParametersNames(parameterNames);

            List<Link> links = Stream.concat(
                    testCase.getLinks().stream(),
                    result.getLinks().stream()
            ).distinct().collect(Collectors.toList());
            testCase.setLinks(links);
        });

        return new Report(plugins, testCases, results);
    }

    private Stream<TestCaseResult> readTestCases(ResultsSource source) {
        return readers.stream()
                .flatMap(reader -> reader.readResults(source).stream());
    }

    private TestCase createTestCase(TestCaseResult result) {
        return new TestCase()
                .withId(result.getId())
                .withName(result.getName())
                .withDescription(result.getDescription())
                .withDescriptionHtml(result.getDescriptionHtml());
    }
}
