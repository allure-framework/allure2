package io.qameta.allure.behaviors;

import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.GroupLink;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.AbstractTreeAggregator;
import io.qameta.allure.tree.TestGroupNode;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.ExtraStatisticMethods.comparator;

/**
 * The plugin adds behaviors tab to the report.
 *
 * @since 2.0
 */
public class BehaviorsPlugin extends AbstractTreeAggregator implements Widget {

    private static final String DEFAULT_FEATURE = "Default feature";
    private static final String DEFAULT_STORY = "Default story";

    @Override
    protected void afterGroupAdded(final TestGroupNode groupNode, final TestResult result) {
        result.getGroupLinks().add(new GroupLink()
                .withGroupType("behaviors")
                .withName(groupNode.getName())
                .withUrl("/#behaviors/" + groupNode.getUid())
        );
    }

    @Override
    protected String getFileName() {
        return "behaviors.json";
    }

    @Override
    protected List<TreeGroup> getGroups(final TestResult result) {
        return Arrays.asList(
                TreeGroup.allByLabel(result, LabelName.FEATURE, DEFAULT_FEATURE),
                TreeGroup.allByLabel(result, LabelName.STORY, DEFAULT_STORY)
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Object getData(final Configuration configuration, final List<LaunchResults> launches) {
        final Map<String, Set<TestResult>> featuresToResults = new HashMap<>();
        for (LaunchResults launchResults : launches) {
            for (TestResult result : launchResults.getResults()) {
                final List<String> features = result.findAll(LabelName.FEATURE);
                for (String feature : features) {
                    final Set<TestResult> featureTestResults = featuresToResults
                            .computeIfAbsent(feature, s -> new HashSet<>());
                    featureTestResults.add(result);
                }
            }
        }

        final List<TreeWidgetItem> items = featuresToResults.entrySet().stream()
                .map(entry -> {
                    Map<String, Status> statusesForStories = getStories(entry.getValue());
                    if (statusesForStories.isEmpty()) {
                        statusesForStories = getDefaultStoryStatus(entry.getValue());
                    }
                    return new TreeWidgetItem()
                            .withStatistic(from(statusesForStories))
                            .withName(entry.getKey());
                })
                .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                .limit(10)
                .collect(Collectors.toList());
        return new TreeWidgetData().withItems(items).withTotal(featuresToResults.entrySet().size());
    }

    @Override
    public String getName() {
        return "behaviors";
    }

    private Map<String, Status> getDefaultStoryStatus(final Set<TestResult> featureTestResults) {
        return Collections.singletonMap(DEFAULT_STORY,
                featureTestResults.stream().map(TestResult::getStatus).reduce(this::min).orElse(Status.UNKNOWN));
    }

    private Map<String, Status> getStories(final Set<TestResult> featureTestResults) {
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

    protected Statistic from(final Map<String, Status> stories) {
        final List<Status> result = stories.entrySet().stream()
                .map(Map.Entry::getValue)
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
