package io.qameta.allure.mail;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * Plugin generates mail with report summary.
 *
 * @since 2.0
 */
public class MailPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailPlugin.class);

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final FreemarkerContext context = configuration.requireContext(FreemarkerContext.class);
        final Path exportFolder = Files.createDirectories(outputDirectory.resolve(Constants.EXPORT_DIR));
        final Path mailFile = exportFolder.resolve("mail.html");
        try (BufferedWriter writer = Files.newBufferedWriter(mailFile)) {
            final Template template = context.getValue().getTemplate("mail.html.ftl");
            template.process(new HashMap<>(), writer);
        } catch (TemplateException e) {
            LOGGER.error("Could't write mail file", e);
        }
    }

}
