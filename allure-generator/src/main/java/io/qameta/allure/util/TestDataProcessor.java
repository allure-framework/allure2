package io.qameta.allure.util;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.Reader;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class TestDataProcessor {

    private final Path resultsDirectory;
    private final Reader resultsReader;

    public TestDataProcessor(final Path resultsDirectory, final Reader reader) {
        this.resultsDirectory = resultsDirectory;
        this.resultsReader = reader;
    }

    public LaunchResults processResources(final String... strings) throws IOException {
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
        resultsReader.readResults(configuration, resultsVisitor, resultsDirectory);
        return resultsVisitor.getLaunchResults();
    }

    private void copyFile(final Path dir, final String resourceName, final String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}
