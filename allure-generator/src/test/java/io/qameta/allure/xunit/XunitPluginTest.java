package io.qameta.allure.xunit;

import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.testdata.TestData;
import io.qameta.allure.tree2.Tree;
import io.qameta.allure.tree2.TreeNode;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class XunitPluginTest {

    @Test
    public void shouldCreateTree() throws Exception {
        final XunitPlugin xunitPlugin = new XunitPlugin();

        final TestResult first = new TestResult()
                .withName("first")
                .withLabels(new Label().withName("suite").withValue("s1"));
        final TestResult second = new TestResult()
                .withName("second")
                .withLabels(new Label().withName("suite").withValue("s1"));
        final TestResult third = new TestResult()
                .withName("third")
                .withLabels(new Label().withName("suite").withValue("s2"));

        Supplier<String> uidGenerator = () -> UUID.randomUUID().toString();
        final Tree<TestResult> tree = xunitPlugin.getData(
                uidGenerator,
                TestData.createSingleLaunchResults(first, second, third)
        );

        assertThat(tree.getChildren())
                .hasSize(2)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("s1", "s2");
    }
}