package io.qameta.allure.mail;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.freemarker.FreemarkerContext;
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
public class MailPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailPlugin.class);

    @Override
    public void aggregate(final ReportConfiguration configuration,
                          final List<LaunchResults> launches,
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
