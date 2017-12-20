package io.qameta.allure.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.entity.TestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTree extends AbstractTree<TestResult, TestResultLeafNode, TestResultGroupNode> {

    public TestResultTree(final String name, final Classifier<TestResult> classifier) {
        super(
                new TestResultGroupNode(null, new GroupNodeContext(null, name)),
                classifier,
                getGroupFactory(),
                getLeafFactory()
        );
    }

    @JsonIgnore
    @Override
    public List<TestResultGroupNode> getGroups() {
        return super.getGroups();
    }

    @JsonIgnore
    @Override
    public List<TestResultLeafNode> getLeafs() {
        return super.getLeafs();
    }

    @JsonProperty
    public List<Node> getChildren() {
        final List<Node> nodes = new ArrayList<>();
        nodes.addAll(getGroups());
        nodes.addAll(getLeafs());
        return nodes;
    }

    private static LeafFactory<TestResult, TestResultLeafNode, TestResultGroupNode> getLeafFactory() {
        return (parent, testResult) -> new TestResultLeafNode(parent.getUid(), testResult);
    }

    private static GroupFactory<TestResultGroupNode> getGroupFactory() {
        return (parent, id) -> new TestResultGroupNode(parent.getUid(), id);
    }
}
