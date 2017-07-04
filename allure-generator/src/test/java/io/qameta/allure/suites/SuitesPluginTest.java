package io.qameta.allure.suites;

import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.testdata.TestData;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeNode;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class SuitesPluginTest {

    @Test
    public void shouldCreateTree() throws Exception {
        final SuitesPlugin xunitPlugin = new SuitesPlugin();

        final TestResult first = new TestResult()
                .withName("first")
                .withLabels(new Label().withName("suite").withValue("s1"));
        final TestResult second = new TestResult()
                .withName("second")
                .withLabels(new Label().withName("suite").withValue("s1"));
        final TestResult third = new TestResult()
                .withName("third")
                .withLabels(new Label().withName("suite").withValue("s2"));

        final Tree<TestResult> tree = xunitPlugin.getData(
                TestData.createSingleLaunchResults(first, second, third)
        );

        assertThat(tree.getChildren())
                .hasSize(2)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("s1", "s2");
    }
}