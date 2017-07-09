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
import org.assertj.core.groups.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class BehaviorsPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void storiesPerFeatureResultsAggregation() throws IOException {
        final Configuration configuration = mock(Configuration.class);

        final Set<TestResult> testResults = new HashSet<>();
        testResults.add(new TestResult()
                .withStatus(Status.PASSED)
                .withLabels(feature("feature1"), feature("feature2"), story("story1"), story("story2")));
        testResults.add(new TestResult()
                .withStatus(Status.FAILED)
                .withLabels(feature("feature2"), feature("feature3"), story("story2"), story("story3")));

        LaunchResults results = new DefaultLaunchResults(testResults, Collections.emptyMap(), Collections.emptyMap());

        TreeWidgetData behaviorsData = (TreeWidgetData) new BehaviorsPlugin().getData(configuration,
                Collections.singletonList(results));

        assertThat(behaviorsData.getItems())
                .filteredOn(node2 -> node2.getName().equals("feature1"))
                .extracting(TreeWidgetItem::getStatistic)
                .extracting(Statistic::getFailed, Statistic::getPassed)
                .containsExactly(Tuple.tuple(0L, 2L));

        assertThat(behaviorsData.getItems())
                .filteredOn(node1 -> node1.getName().equals("feature2"))
                .extracting(TreeWidgetItem::getStatistic)
                .extracting(Statistic::getFailed, Statistic::getPassed)
                .containsExactly(Tuple.tuple(2L, 1L));

        assertThat(behaviorsData.getItems())
                .filteredOn(node -> node.getName().equals("feature3"))
                .extracting(TreeWidgetItem::getStatistic)
                .extracting(Statistic::getFailed, Statistic::getPassed)
                .containsExactly(Tuple.tuple(2L, 0L));
    }


    private Label feature(final String value) {
        return new Label().withName("feature").withValue(value);
    }

    private Label story(final String value) {
        return new Label().withName("story").withValue(value);
    }


}
