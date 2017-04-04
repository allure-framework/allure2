package io.qameta.allure.launch;

import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Statistic;

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
public class LaunchPlugin implements Reader, Widget {

    private static final String LAUNCH_BLOCK_NAME = "launch";
    private static final String LAUNCH_JSON = "launch.json";

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path executorFile = directory.resolve(LAUNCH_JSON);
        if (Files.exists(executorFile)) {
            try (InputStream is = Files.newInputStream(executorFile)) {
                final LaunchInfo info = context.getValue().readValue(is, LaunchInfo.class);
                visitor.visitExtra(LAUNCH_BLOCK_NAME, info);
            } catch (IOException e) {
                visitor.error("Could not read launch file " + executorFile, e);
            }
        }
    }

    @Override
    public Object getData(final Configuration configuration,
                          final List<LaunchResults> launches) {
        return launches.stream()
                .map(this::updateLaunchInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<LaunchInfo> updateLaunchInfo(final LaunchResults results) {
        final Optional<LaunchInfo> extra = results.getExtra(LAUNCH_BLOCK_NAME);
        extra.map(launchInfo -> {
            final Statistic statistic = new Statistic();
            launchInfo.setStatistic(statistic);
            results.getResults().forEach(statistic::update);
            return launchInfo;
        });
        return extra;
    }

    @Override
    public String getName() {
        return "launches";
    }
}
