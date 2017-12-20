package io.qameta.allure.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class TestResultGroupNode implements GroupNode<TestResultLeafNode, TestResultGroupNode> {

    private final String uid;
    private final GroupNodeContext context;

    private final List<TestResultGroupNode> groups = new ArrayList<>();
    private final List<TestResultLeafNode> leafs = new ArrayList<>();

    public TestResultGroupNode(final String parentUid, final GroupNodeContext context) {
        this.uid = TreeUtils.createGroupUid(parentUid, context);
        this.context = context;
    }

    @Override
    public String getName() {
        return context.getValue();
    }

    @JsonIgnore
    @Override
    public List<TestResultGroupNode> getGroups() {
        return groups;
    }

    @JsonIgnore
    @Override
    public List<TestResultLeafNode> getLeafs() {
        return leafs;
    }

    @JsonProperty
    public List<Node> getChildren() {
        final List<Node> nodes = new ArrayList<>();
        nodes.addAll(getGroups());
        nodes.addAll(getLeafs());
        return nodes;
    }
}
