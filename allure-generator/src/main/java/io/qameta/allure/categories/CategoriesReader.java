package io.qameta.allure.categories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.TestRunDetailsReader;
import io.qameta.allure.entity.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static io.qameta.allure.categories.CategoriesPlugin.CATEGORIES;
import static io.qameta.allure.categories.CategoriesPlugin.CATEGORIES_JSON;

/**
 * @author charlie (Dmitry Baev).
 */
public class CategoriesReader implements TestRunDetailsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoriesReader.class);

    //@formatter:off
    private static final TypeReference<List<Category>> CATEGORIES_TYPE =
        new TypeReference<List<Category>>() {};
    //@formatter:on

    private final ObjectMapper mapper;

    @Inject
    public CategoriesReader(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Consumer<TestRun> readDetails(final Path source) {
        return testRun -> {
            final Path file = source.resolve(CATEGORIES_JSON);
            if (Files.exists(file)) {
                try (InputStream is = Files.newInputStream(file)) {
                    final List<Category> categories = mapper.readValue(is, CATEGORIES_TYPE);
                    testRun.addExtraBlock(CATEGORIES, categories);
                } catch (IOException e) {
                    LOGGER.error("Could not read categories file {}", file, e);
                }
            }
        };
    }
}
