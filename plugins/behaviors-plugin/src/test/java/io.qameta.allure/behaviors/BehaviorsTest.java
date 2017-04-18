package io.qameta.allure.behaviors;

import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class BehaviorsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void storiesPerFeatureResultsAggregation() throws IOException {
        final Configuration configuration = mock(Configuration.class);
        final Statistic feature1 = new Statistic()
                .withPassed(2);
        final Statistic feature2 = new Statistic()
                .withPassed(1)
                .withFailed(2);
        final Statistic feature3 = new Statistic()
                .withFailed(2);

        final Set<TestResult> testResults = new HashSet<>();
        testResults.add(new TestResult()
                .withStatus(Status.PASSED)
                .withLabels(
                        new Label().withName("feature").withValue("feature1"),
                        new Label().withName("feature").withValue("feature2"),
                        new Label().withName("story").withValue("story1"),
                        new Label().withName("story").withValue("story2")
                ));
        testResults.add(new TestResult()
                .withStatus(Status.FAILED)
                .withLabels(
                        new Label().withName("feature").withValue("feature2"),
                        new Label().withName("feature").withValue("feature3"),
                        new Label().withName("story").withValue("story2"),
                        new Label().withName("story").withValue("story3")
                ));

        LaunchResults results = new DefaultLaunchResults(testResults, Collections.emptyMap(), Collections.emptyMap());

        TreeWidgetData behaviorsData = (TreeWidgetData) new BehaviorsPlugin().getData(configuration,
                Collections.singletonList(results));
        assertStatisticForFeature(behaviorsData.getItems(), "feature1", feature1);
        assertStatisticForFeature(behaviorsData.getItems(), "feature2", feature2);
        assertStatisticForFeature(behaviorsData.getItems(), "feature3", feature3);
    }

    @Test
    public void shouldAddOneDefaultFeatureForStory() {
        final Configuration configuration = mock(Configuration.class);
        final Statistic featureStats = new Statistic()
                .withFailed(1);
        final String featureName = "featureName";


        final Set<TestResult> testResults = new HashSet<>();
        testResults.add(new TestResult()
                .withStatus(Status.PASSED)
                .withLabels(
                        new Label().withName("feature").withValue(featureName)
                ));
        testResults.add(new TestResult()
                .withStatus(Status.FAILED)
                .withLabels(
                        new Label().withName("feature").withValue(featureName)
                ));

        LaunchResults results = new DefaultLaunchResults(testResults, Collections.emptyMap(), Collections.emptyMap());

        TreeWidgetData behaviorsData = (TreeWidgetData) new BehaviorsPlugin().getData(configuration,
                Collections.singletonList(results));
        assertStatisticForFeature(behaviorsData.getItems(), featureName, featureStats);
    }

    private void assertStatisticForFeature(final List<TreeWidgetItem> items, final String featureName,
                                           final Statistic expected) {
        assertThat(items)
                .filteredOn(node -> node.getName().equals(featureName))
                .extracting(TreeWidgetItem::getStatistic).first()
                .as("Unexpected aggregated statistic for feature %s", featureName)
                .isEqualToComparingFieldByField(expected);
    }
}
