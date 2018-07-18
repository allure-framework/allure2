package io.qameta.allure.tags;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class TagsPlugin implements Aggregator {

    public static final String TAGS_BLOCK_NAME = "tags";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(result -> {
                    final Set<String> tags = new HashSet<>(result.findAllLabels(LabelName.TAG));
                    result.addExtraBlock(TAGS_BLOCK_NAME, tags);
                });
    }
}
