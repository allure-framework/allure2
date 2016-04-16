package org.allurefw.report.behaviors;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.BehaviorData;
import org.allurefw.report.Feature;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginScope;
import org.allurefw.report.ReportApiUtils;
import org.allurefw.report.Story;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCase;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
@Plugin(name = "behaviors", scope = PluginScope.PROCESS)
public class BehaviorsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        BehaviorData behaviorData = new BehaviorData();

//        aggregator(behaviorData, this::aggregate);
//        reportData(behaviorData);
    }

    protected void aggregate(BehaviorData identity, TestCase testCase) {
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
