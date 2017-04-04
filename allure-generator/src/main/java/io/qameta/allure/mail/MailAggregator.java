package io.qameta.allure.mail;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class MailAggregator implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailAggregator.class);

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final FreemarkerContext context = configuration.requireContext(FreemarkerContext.class);
        final Path exportFolder = Files.createDirectories(outputDirectory.resolve("export"));
        final Path mailFile = exportFolder.resolve("mail.html");
        try (BufferedWriter writer = Files.newBufferedWriter(mailFile, StandardOpenOption.CREATE)) {
            final Template template = context.getValue().getTemplate("mail.html.ftl");
            template.process(new HashMap<>(), writer);
        } catch (TemplateException e) {
            LOGGER.error("Could't write mail file", e);
        }
    }

}
