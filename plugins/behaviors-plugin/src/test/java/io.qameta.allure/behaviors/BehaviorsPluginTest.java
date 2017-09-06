package io.qameta.allure.behaviors;

import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.Issue;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;
import org.assertj.core.groups.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.qameta.allure.entity.LabelName.EPIC;
import static io.qameta.allure.entity.LabelName.FEATURE;
import static io.qameta.allure.entity.LabelName.STORY;
import static java.util.Arrays.asList;
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
                .setStatus(Status.PASSED)
                .setLabels(asList(FEATURE.label("feature1"), FEATURE.label("feature2"), STORY.label("story1"), STORY.label("story2"))));
        testResults.add(new TestResult()
                .setStatus(Status.FAILED)
                .setLabels(asList(FEATURE.label("feature2"), FEATURE.label("feature3"), STORY.label("story2"), STORY.label("story3"))));

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

    @Test
    public void shouldGroupByEpic() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final Set<TestResult> testResults = new HashSet<>();
        testResults.add(new TestResult()
                .setStatus(Status.PASSED)
                .setLabels(asList(EPIC.label("e1"), FEATURE.label("f1"), STORY.label("s1"))));
        testResults.add(new TestResult()
                .setStatus(Status.FAILED)
                .setLabels(asList(EPIC.label("e2"), FEATURE.label("f2"), STORY.label("s2"))));

        LaunchResults results = new DefaultLaunchResults(testResults, Collections.emptyMap(), Collections.emptyMap());

        TreeWidgetData behaviorsData = (TreeWidgetData) new BehaviorsPlugin().getData(configuration,
                Collections.singletonList(results));

        assertThat(behaviorsData.getItems())
                .extracting("name")
                .containsExactlyInAnyOrder("e1", "e2");
    }

    @Issue("587")
    @Issue("572")
    @Test
    public void shouldSortByStartTimeAsc() throws Exception {
        final TestResult first = new TestResult()
                .setName("first")
                .setTime(new Time().setStart(10L));
        final TestResult second = new TestResult()
                .setName("second")
                .setTime(new Time().setStart(12L));
        final TestResult timeless = new TestResult()
                .setName("timeless");

        final LaunchResults results = new DefaultLaunchResults(
                new HashSet<>(Arrays.asList(first, second, timeless)),
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        final BehaviorsPlugin plugin = new BehaviorsPlugin();
        final Tree<TestResult> tree = plugin.getData(Collections.singletonList(results));

        assertThat(tree.getChildren())
                .extracting(TreeNode::getName)
                .containsExactly("timeless", "first", "second");
    }
}

