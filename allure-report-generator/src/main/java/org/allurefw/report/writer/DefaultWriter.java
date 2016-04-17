package org.allurefw.report.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.allurefw.report.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class DefaultWriter implements Writer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWriter.class);

    private final ObjectMapper mapper;

    @Inject
    public DefaultWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void write(Path outputDirectory, String fileName, Object object) {
        if (createDirectories(outputDirectory)) {
            return;
        }

        Path dest = outputDirectory.resolve(fileName);
        try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(dest))) {
            Files.createDirectories(dest.getParent());
            mapper.writeValue(stream, object);
        } catch (IOException e) {
            LOGGER.warn("Couldn't write {} to {}: {}", object.getClass(), dest, e);
        }
    }

    @Override
    public void write(Path outputDirectory, String fileName, Path source) {
        if (createDirectories(outputDirectory)) {
            return;
        }

        Path dest = outputDirectory.resolve(fileName);
        try {
            Files.createDirectories(dest.getParent());
            Files.copy(source, dest);
        } catch (IOException e) {
            LOGGER.error("Couldn't copy file {} to {}: {}", source, dest, e);
        }
    }

    @Override
    public void writeIndexHtml(Path outputDirectory, Set<String> pluginNames) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "tpl");

        if (createDirectories(outputDirectory)) {
            return;
        }
        Path indexHtml = outputDirectory.resolve("index.html");
        try (BufferedWriter writer = Files.newBufferedWriter(indexHtml)) {
            Template template = cfg.getTemplate("index.html.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("plugins", pluginNames);
            template.process(dataModel, writer);
        } catch (IOException | TemplateException e) {
            LOGGER.error("Could't process index file", e);
        }
    }

    private boolean createDirectories(Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            LOGGER.error("Couldn't create output directory {}: {}", outputDirectory, e);
            return true;
        }
        return false;
    }

}
