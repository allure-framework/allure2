package io.qameta.allure.behaviors;

import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.AbstractTreeAggregator;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The plugin adds behaviors tab to the report.
 *
 * @since 2.0
 */
public class BehaviorsPlugin extends AbstractTreeAggregator implements Widget {

    @Override
    protected String getFileName() {
        return "behaviors.json";
    }

    @Override
    protected List<TreeGroup> getGroups(final TestResult result) {
        return Arrays.asList(
                TreeGroup.allByLabel(result, LabelName.FEATURE, "Default feature"),
                TreeGroup.allByLabel(result, LabelName.STORY, "Default story")
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Object getData(final Configuration configuration, final List<LaunchResults> launches) {
        final Map<String, Set<TestResult>> featureStory = new HashMap<>();
        for (LaunchResults launchResults : launches) {
            for (TestResult result : launchResults.getResults()) {
                final List<String> features = result.findAll(LabelName.FEATURE);
                for (String feature : features) {
                    final Set<TestResult> featureTestResults = featureStory
                            .computeIfAbsent(feature, s -> new HashSet<>());
                    featureTestResults.add(result);
                }
            }
        }

        final List<TreeWidgetItem> items = featureStory.entrySet().stream()
                .map(entry -> {
                    final Set<String> stories = entry.getValue().stream().flatMap(
                            result -> result.findAll(LabelName.STORY).stream()).collect(Collectors.toSet());
                    final Map<String, Status> statusesForStories = getStories(entry.getValue());
                    return new TreeWidgetItem()
                            .withStatistic(from(stories, statusesForStories))
                            .withName(entry.getKey());
                })
                .collect(Collectors.toList());
        return new TreeWidgetData().withItems(items).withTotal(items.size());
    }

    @Override
    public String getName() {
        return "behaviors";
    }

    protected Map<String, Status> getStories(final Set<TestResult> featureTestResults) {
        final Map<String, Status> storyStatus = new HashMap<>();
        featureTestResults.forEach(result -> {
                    final List<String> stories = result.findAll(LabelName.STORY);
                    stories.forEach(story -> {
                        storyStatus.putIfAbsent(story, result.getStatus());
                        storyStatus.computeIfPresent(story, (key, value) -> min(value, result.getStatus()));
                    });
                });

        return storyStatus;
    }

    protected Statistic from(final Set<String> names, final Map<String, Status> statuses) {
        final List<Status> result = names.stream()
                .map(statuses::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return from(result);
    }

    protected Statistic from(final List<Status> statuses) {
        final Statistic statistic = new Statistic();
        statuses.forEach(statistic::update);
        return statistic;
    }

    protected Status min(final Status first, final Status second) {
        return Stream.of(first, second)
                .min(Status::compareTo)
                .orElse(Status.UNKNOWN);
    }
}
