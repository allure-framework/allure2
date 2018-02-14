package io.qameta.allure.tree;

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

    @Override
    public List<TestResultGroupNode> getGroups() {
        return groups;
    }

    @Override
    public List<TestResultLeafNode> getLeafs() {
        return leafs;
    }

}
