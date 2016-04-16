package org.allurefw.report.behaviors;

import org.allurefw.report.Aggregator;
import org.allurefw.report.BehaviorData;
import org.allurefw.report.Feature;
import org.allurefw.report.ReportApiUtils;
import org.allurefw.report.Story;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCase;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 16.04.16
 */
public class BehaviorsAggregator implements Aggregator<BehaviorData> {

    @Override
    public Supplier<BehaviorData> supplier() {
        return BehaviorData::new;
    }

    @Override
    public BinaryOperator<BehaviorData> combiner() {
        return (left, right) -> left.withFeatures(right.getFeatures());
    }

    @Override
    public BiConsumer<BehaviorData, TestCase> accumulator() {
        return (identity, testCase) -> {
            Set<String> features = getLabelValues(testCase, LabelName.FEATURE, "Default feature");
            Set<String> stories = getLabelValues(testCase, LabelName.STORY, "Default story");

            features.forEach(featureName -> {
                Feature feature = identity.getFeatures().stream()
                        .filter(item -> featureName.equals(item.getName()))
                        .findAny()
                        .orElseGet(() -> newFeature(identity, featureName));

                stories.forEach(storyName -> {
                    Story story = feature.getStories().stream()
                            .filter(item -> storyName.equals(item.getName()))
                            .findAny()
                            .orElseGet(() -> newStory(feature, storyName));
                    story.getTestCases().add(testCase.toInfo());
                });
            });
        };
    }

    protected Feature newFeature(BehaviorData identity, String featureName) {
        Feature newOne = new Feature().withName(featureName);
        identity.getFeatures().add(newOne);
        return newOne;
    }

    protected Story newStory(Feature feature, String storyName) {
        Story newOne = new Story().withName(storyName)
                .withUid(ReportApiUtils.generateUid());
        feature.getStories().add(newOne);
        return newOne;
    }

    protected Set<String> getLabelValues(TestCase testCase, LabelName labelName, String defaultValue) {
        Set<String> result = testCase.findAll(labelName, Collectors.toSet());
        return result.isEmpty() ? Collections.singleton(defaultValue) : result;
    }
}
