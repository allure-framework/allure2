package io.qameta.allure.tree;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.TestResult;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.FEATURE;
import static io.qameta.allure.entity.LabelName.STORY;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeTest {

    @Test
    public void shouldCreateEmptyTree() throws Exception {
        final Tree<String> tree = new DefaultTree<>(
                "default",
                s -> Collections.emptyList(),
                s -> Optional.empty()
        );

        assertThat(tree.getChildren())
                .hasSize(0);

        final ObjectMapper mapper = new ObjectMapper();
        final String value = mapper.writeValueAsString(tree);
        System.out.println(value);
    }

    @Test
    public void shouldSkipItemsWithoutClassifier() throws Exception {
        final Tree<String> tree = new DefaultTree<>(
                "default",
                s -> Collections.emptyList(),
                s -> Optional.of(new DefaultTreeLeaf(s))
        );

        tree.add("hello");
        tree.add("hey");
        tree.add("wo");

        assertThat(tree.getChildren())
                .hasSize(3);
    }

    @Test
    public void shouldAddItems() throws Exception {
        final Tree<String> tree = new DefaultTree<>(
                "default",
                this::byLetters,
                s -> Optional.of(new DefaultTreeLeaf(s))
        );

        tree.add("hello");
        tree.add("hey");
        tree.add("wo");

        assertThat(tree.getChildren())
                .hasSize(2)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("h", "w");
    }

    @Test
    public void shouldCrossGroup() throws Exception {
        final Tree<TestResult> behaviors = new DefaultTree<>(
                "behaviors",
                testResult -> groupByLabels(testResult, FEATURE, STORY),
                TestResultTreeLeaf::create
        );

        final TestResult first = new TestResult()
                .withName("first")
                .withLabels(feature("f1"), feature("f2"), story("s1"), story("s2"));
        final TestResult second = new TestResult()
                .withName("second")
                .withLabels(feature("f2"), feature("f3"), story("s2"), story("s3"));
        behaviors.add(first);
        behaviors.add(second);

        assertThat(behaviors.getChildren())
                .hasSize(3)
                .extracting(TreeNode::getName)
                .containsExactlyInAnyOrder("f1", "f2", "f3");

        assertThat(behaviors.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("s1", "s2", "s1", "s2", "s3", "s2", "s3");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("s1", "s2", "s3");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s1")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s2")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first", "second");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s3")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("second");
    }

    private List<Classifier<String>> byLetters(final String item) {
        return item.chars()
                .mapToObj(value -> String.valueOf((char) value))
                .map(String::valueOf)
                .map(string -> new Classifier<String>() {
                    @Override
                    public List<String> classify(final String item) {
                        return Collections.singletonList(string);
                    }

                    @Override
                    public TreeGroup factory(final String name, final String item) {
                        return new DefaultTreeGroup(name);
                    }
                })
                .collect(Collectors.toList());
    }

    private Label feature(final String value) {
        return new Label().withName("feature").withValue(value);
    }

    private Label story(final String value) {
        return new Label().withName("story").withValue(value);
    }

}