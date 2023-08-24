/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.mail;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator2;
import io.qameta.allure.Constants;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 * Plugin generates mail with report summary.
 *
 * @since 2.0
 */
public class MailPlugin implements Aggregator2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailPlugin.class);

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        final FreemarkerContext context = configuration.requireContext(FreemarkerContext.class);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            final Template template = context.getValue().getTemplate("mail.html.ftl");
            template.process(new HashMap<>(), writer);
            storage.addDataBinary(
                    Constants.exportPath("mail.html"),
                    bos.toByteArray()
            );
        } catch (TemplateException e) {
            LOGGER.error("Couldn't write mail file", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
