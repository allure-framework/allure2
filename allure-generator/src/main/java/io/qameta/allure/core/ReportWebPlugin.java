package io.qameta.allure.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.service.TestResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Plugins that stores report static files to data directory.
 *
 * @since 2.0
 */
public class ReportWebPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportWebPlugin.class);

    public static final String BASE_PACKAGE_PATH = "tpl";

    private final List<String> staticFiles = Arrays.asList("app.js", "styles.css", "favicon.ico");

    @Override
    public void aggregate(final ReportContext context,
                          final TestResultService service,
                          final Path outputDirectory) throws IOException {
        writeIndexHtml(outputDirectory);
        writeStatic(outputDirectory);
    }

    protected void writeIndexHtml(final Path outputDirectory) throws IOException {
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setLocalizedLookup(false);
        configuration.setTemplateUpdateDelayMilliseconds(0);
        configuration.setClassLoaderForTemplateLoading(getClass().getClassLoader(), BASE_PACKAGE_PATH);

        final Path indexHtml = outputDirectory.resolve("index.html");
        try (BufferedWriter writer = Files.newBufferedWriter(indexHtml)) {
            final Template template = configuration.getTemplate("index.html.ftl");
            final Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("plugins", Collections.emptyEnumeration());
            template.process(dataModel, writer);
        } catch (TemplateException e) {
            LOGGER.error("Could't write index file", e);
        }
    }

    protected void writeStatic(final Path outputDirectory) {
        staticFiles.forEach(resourceName -> {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                if (Objects.isNull(is)) {
                    LOGGER.info("Could not find resource {}", resourceName);
                    return;
                }
                Files.copy(is, outputDirectory.resolve(resourceName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Couldn't unpack report static");
            }
        });
    }
}
