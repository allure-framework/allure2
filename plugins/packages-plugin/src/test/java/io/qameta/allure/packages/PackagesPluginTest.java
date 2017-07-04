package io.qameta.allure.packages;

import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.Tree;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class PackagesPluginTest {

    @Test
    public void shouldCreateTree() throws Exception {
        final Set<TestResult> testResults = new HashSet<>();

        final TestResult first = new TestResult().withName("first").withLabels(
                packageLabel("a.b"),
                new Label().withName("testMethod").withValue("firstMethod")
        );
        final TestResult second = new TestResult().withName("second").withLabels(packageLabel("a.c"));
        testResults.add(first);
        testResults.add(second);

        final LaunchResults results = new DefaultLaunchResults(
                testResults,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        final PackagesPlugin packagesPlugin = new PackagesPlugin();
        final Tree<TestResult> tree = packagesPlugin.getData(Collections.singletonList(results));

        assertThat(tree.getChildren())
                .hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("a");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("b", "c");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("firstMethod", "second");
    }

    @Test
    public void shouldCollapseNodesWithOneChild() throws Exception {
        final Set<TestResult> testResults = new HashSet<>();

        final TestResult first = new TestResult().withName("first").withLabels(packageLabel("a.b.c"));
        final TestResult second = new TestResult().withName("second").withLabels(packageLabel("a.d.e"));
        testResults.add(first);
        testResults.add(second);

        final LaunchResults results = new DefaultLaunchResults(
                testResults,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        final PackagesPlugin packagesPlugin = new PackagesPlugin();
        final Tree<TestResult> tree = packagesPlugin.getData(Collections.singletonList(results));

        assertThat(tree.getChildren())
                .hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("a");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("b.c", "d.e");

        assertThat(tree.getChildren())
                .flatExtracting("children")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first", "second");
    }

    private static Label packageLabel(final String value) {
        return new Label().withName("package").withValue(value);
    }
}