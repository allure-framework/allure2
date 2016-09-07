package org.allurefw.report;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final ResultsSourceFactory factory;

    private final Set<ResultsReader> readers;

    @Inject
    public Main(ResultsSourceFactory factory, Set<ResultsReader> readers) {
        this.factory = factory;
        this.readers = readers;
    }

    public void process(Path... resultsDirectories) {
        processSources(Stream.of(resultsDirectories)
                .map(factory::create)
                .toArray(ResultsSource[]::new));
    }

    public void processSources(ResultsSource... sources) {
        Stream.of(sources)
                .flatMap(this::readSource)
                .forEach(result -> LOGGER.info("Process result {}", result.getTestCaseResult().getName()));
    }

    private Stream<Result> readSource(ResultsSource source) {
        return readers.stream()
                .flatMap(reader -> reader.readResults(source).stream());
    }

    public static void main(String[] args) {
        Main stage = Guice.createInjector(new ParentModule(Collections.emptyList()))
                .getInstance(Main.class);
        stage.process(Paths.get("/Users/charlie/projects/allure-report/generator/src/test/resources/allure1data"));
    }
}
