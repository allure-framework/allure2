package io.qameta.allure.allure1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.allurefw.allure1.AllureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.allurefw.allure1.AllureUtils.unmarshalTestSuite;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class Allure1FilesReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure1FilesReader.class);

    private final Path source;
    private final ObjectMapper mapper = new ObjectMapper();


    public Allure1FilesReader(final Path source) {
        this.source = source;
    }

    public Stream<TestSuiteResult> getStreamOfAllure1Results() {
        return Stream.concat(xmlFiles(source), jsonFiles(source));
    }

    private Stream<TestSuiteResult> xmlFiles(final Path source) {
        try {
            return AllureUtils.listTestSuiteXmlFiles(source)
                    .stream()
                    .map(this::readXmlTestSuiteFile)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (IOException e) {
            LOGGER.error("Could not list allure1 xml files", e);
            return Stream.empty();
        }
    }

    private Stream<TestSuiteResult> jsonFiles(final Path source) {
        try {
            return AllureUtils.listTestSuiteJsonFiles(source)
                    .stream()
                    .map(this::readJsonTestSuiteFile)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (IOException e) {
            LOGGER.error("Could not list allure1 json files", e);
            return Stream.empty();
        }
    }

    private Optional<TestSuiteResult> readXmlTestSuiteFile(final Path source) {
        try {
            return Optional.of(unmarshalTestSuite(source));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {}: {}", source, e);
        }
        return Optional.empty();
    }

    private Optional<TestSuiteResult> readJsonTestSuiteFile(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Optional.of(mapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {}: {}", source, e);
            return Optional.empty();
        }
    }
}
