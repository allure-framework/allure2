package io.qameta.allure.tree;

import io.qameta.allure.ResultAggregator;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.qameta.allure.ReportApiUtils.generateUid;

/**
 * @author charlie (Dmitry Baev).
 */
public abstract class TreeResultAggregator implements ResultAggregator<TreeData> {

    @Override
    public Supplier<TreeData> supplier(final TestRun testRun, final TestCase testCase) {
        return TreeData::new;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Consumer<TreeData> aggregate(final TestRun testRun, final TestCase testCase, final TestCaseResult result) {
        return treeData -> {
            if (!shouldAggregate(result)) {
                return;
            }

            treeData.updateStatistic(result);
            treeData.updateTime(result);

            List<WithChildren> currentLevelGroups = Collections.singletonList(treeData);

            for (TreeGroup treeGroup : getGroups(result)) {
                if (treeGroup.getGroupNames().isEmpty()) {
                    continue;
                }

                final List<WithChildren> nextLevelGroups = new ArrayList<>();
                for (WithChildren currentLevelGroup : currentLevelGroups) {
                    for (String groupName : treeGroup.getGroupNames()) {
                        TestGroupNode groupNode = findGroupByName(groupName, currentLevelGroup.getChildren());
                        groupNode.updateStatistic(result);
                        groupNode.updateTime(result);
                        nextLevelGroups.add(groupNode);
                    }
                }
                currentLevelGroups = nextLevelGroups;
            }
            final boolean isFlaky = Optional.ofNullable(result.getStatusDetails())
                    .map(StatusDetails::isFlaky)
                    .orElse(false);
            TestCaseNode testCaseNode = new TestCaseNode()
                    .withUid(result.getUid())
                    .withName(getNodeName(result))
                    .withStatus(result.getStatus())
                    .withTime(result.getTime())
                    .withFlaky(isFlaky);
            for (WithChildren currentLevelGroup : currentLevelGroups) {
                currentLevelGroup.getChildren().add(testCaseNode);
            }
        };
    }

    protected TestGroupNode findGroupByName(final String groupName, final List<TreeNode> nodes) {
        return nodes.stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .filter(group -> Objects.equals(groupName, group.getName()))
                .findAny()
                .orElseGet(() -> {
                    TestGroupNode newOne = new TestGroupNode()
                            .withName(groupName)
                            .withUid(generateUid());
                    nodes.add(newOne);
                    return newOne;
                });
    }

    protected boolean shouldAggregate(final TestCaseResult result) {
        return true;
    }

    protected abstract List<TreeGroup> getGroups(final TestCaseResult result);

    protected String getNodeName(final TestCaseResult result) {
        return result.getName();
    }
}
