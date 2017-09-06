package io.qameta.allure.suites;

import io.qameta.allure.Issue;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import org.junit.Test;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class SuitesPluginTest {

    @Test
    public void shouldCreateTree() throws Exception {
        final SuitesPlugin plugin = new SuitesPlugin();

        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(singletonList(new Label().setName("suite").setValue("s1")));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(singletonList(new Label().setName("suite").setValue("s1")));
        final TestResult third = new TestResult()
                .setName("third")
                .setLabels(singletonList(new Label().setName("suite").setValue("s2")));

        final Tree<TestResult> tree = plugin.getData(
                createSingleLaunchResults(first, second, third)
        );

        assertThat(tree.getChildren())
                .hasSize(2)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("s1", "s2");
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

        final SuitesPlugin plugin = new SuitesPlugin();
        final Tree<TestResult> tree = plugin.getData(
                createSingleLaunchResults(second, first, timeless)
        );

        assertThat(tree.getChildren())
                .extracting(TreeNode::getName)
                .containsExactly("timeless", "first", "second");
    }
}