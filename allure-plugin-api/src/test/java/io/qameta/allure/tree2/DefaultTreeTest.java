package io.qameta.allure.tree2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
}