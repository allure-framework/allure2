package io.qameta.allure.executor;

import io.qameta.allure.CommonWidgetAggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorPlugin extends CommonWidgetAggregator implements Reader {

    public static final String EXECUTORS_BLOCK_NAME = "executor";
    protected static final String JSON_FILE_NAME = "executor.json";

    public ExecutorPlugin() {
        super("executors.json");
    }

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path executorFile = directory.resolve(JSON_FILE_NAME);
        if (Files.exists(executorFile)) {
            try (InputStream is = Files.newInputStream(executorFile)) {
                final ExecutorInfo info = context.getValue().readValue(is, ExecutorInfo.class);
                visitor.visitExtra(EXECUTORS_BLOCK_NAME, info);
            } catch (IOException e) {
                visitor.error("Could not read executor file " + executorFile, e);
            }
        }
    }

    @Override
    public WidgetCollection<ExecutorInfo> getData(final Configuration configuration,
                                                  final List<LaunchResults> launches) {
        List<ExecutorInfo> executorInfos = getData(launches);
        return new WidgetCollection<>(executorInfos.size(), executorInfos);
    }

    private List<ExecutorInfo> getData(final List<LaunchResults> launches) {
        return launches.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .collect(Collectors.toList());
    }
}
