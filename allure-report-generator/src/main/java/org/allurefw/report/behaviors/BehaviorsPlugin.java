package org.allurefw.report.behaviors;

import org.allurefw.LabelName;
import org.allurefw.report.BehaviorData;
import org.allurefw.report.Feature;
import org.allurefw.report.Story;
import org.allurefw.report.TestCase;
import org.allurefw.report.TestCaseProcessor;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class BehaviorsPlugin implements TestCaseProcessor {

    @Inject
    protected BehaviorData data;

    @Override
    public void process(TestCase testCase) {
        Set<String> features = getLabelValues(testCase, LabelName.FEATURE, "Default feature");
        Set<String> stories = getLabelValues(testCase, LabelName.STORY, "Default story");

        features.forEach(featureName -> {
            Feature feature = data.getFeatures().stream()
                    .filter(featureName::equals)
                    .findAny()
                    .orElseGet(() -> {
                        Feature newOne = new Feature().withTitle(featureName);
                        data.getFeatures().add(newOne);
                        return newOne;
                    });

            stories.forEach(storyName -> {
                Story story = feature.getStories().stream()
                        .filter(storyName::equals)
                        .findAny()
                        .orElseGet(() -> {
                            Story newOne = new Story().withTitle(storyName);
                            feature.getStories().add(newOne);
                            return newOne;
                        });
                story.getTestCases().add(testCase.toInfo());
            });
        });
    }

    protected Set<String> getLabelValues(TestCase testCase, LabelName labelName, String defaultValue) {
        Set<String> result = testCase.findAll(labelName).stream()
                .collect(Collectors.toSet());
        if (result.isEmpty()) {
            result.add(defaultValue);
        }
        return result;
    }
}
