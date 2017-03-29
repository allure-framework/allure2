package io.qameta.allure.executor;

import io.qameta.allure.JacksonMapperContext;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorReader implements ResultsReader {

    @Override
    public void readResults(ReportConfiguration configuration, ResultsVisitor visitor, Path directory) {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path executorFile = directory.resolve("executor.json");
        if (Files.exists(executorFile)) {
            try (InputStream is = Files.newInputStream(executorFile)) {
                final ExecutorInfo info = context.getValue().readValue(is, ExecutorInfo.class);
                visitor.visitExtra("executor", info);
            } catch (IOException e) {
                visitor.error("Could not read executor file " + executorFile, e);
            }
        }
    }
}
