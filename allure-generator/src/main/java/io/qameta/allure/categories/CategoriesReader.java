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

import static io.qameta.allure.categories.CategoriesPlugin.CATEGORIES_JSON;
import static io.qameta.allure.categories.CategoriesPlugin.CATEGORIES;

/**
 * @author charlie (Dmitry Baev).
 */
public class CategoriesReader implements TestRunDetailsReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(CategoriesReader.class);

    private static final TypeReference<List<Category>> CATEGORIES_TYPE =
            new TypeReference<List<Category>>() {
            };

    private final ObjectMapper mapper;

    @Inject
    public CategoriesReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Consumer<TestRun> readDetails(Path source) {
        return testRun -> {
            Path file = source.resolve(CATEGORIES_JSON);
            if (Files.exists(file)) {
                try (InputStream is = Files.newInputStream(file)) {
                    List<Category> categories = mapper.readValue(is, CATEGORIES_TYPE);
                    testRun.addExtraBlock(CATEGORIES, categories);
                } catch (IOException e) {
                    LOGGER.error("Could not read categories file {}", file, e);
                }
            }
        };
    }
}
