package io.qameta.allure.tree;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.Time;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author charlie (Dmitry Baev).
 */
public class TreeResultAggregatorTest {

    @Test
    public void shouldSupplyData() throws Exception {
        OneValueTreeResultAggregator aggregator = new OneValueTreeResultAggregator();
        TreeData treeData = aggregator.supplier(null, null).get();
        assertThat(treeData, notNullValue());
        assertThat(treeData.getChildren(), hasSize(0));
    }

    @Test
    public void shouldAggregate() throws Exception {
        OneValueTreeResultAggregator aggregator = new OneValueTreeResultAggregator();
        TreeData treeData = aggregator.supplier(null, null).get();
        TestCaseResult result = mock(TestCaseResult.class);

        aggregator.accumulator().accept(treeData, result);
        assertThat(treeData, notNullValue());
        assertThat(treeData.getChildren(), hasSize(1));
        assertThat(treeData.getTime(), notNullValue());
        assertThat(treeData.getStatistic(), notNullValue());
    }

    @Test
    public void shouldUpdateStatistic() throws Exception {
        OneValueTreeResultAggregator aggregator = new OneValueTreeResultAggregator();
        TreeData treeData = aggregator.supplier(null, null).get();
        TestCaseResult result = mock(TestCaseResult.class);
        doReturn(Status.PASSED).when(result).getStatus();
        aggregator.accumulator().accept(treeData, result);
        assertThat(treeData.getStatistic(), notNullValue());
        assertThat(treeData.getStatistic().getPassed(), is(1L));
        assertThat(treeData.getStatistic().getTotal(), is(1L));
    }

    @Test
    public void shouldUpdateGroupTime() throws Exception {
        OneValueTreeResultAggregator aggregator = new OneValueTreeResultAggregator();
        TreeData treeData = aggregator.supplier(null, null).get();

        TestCaseResult first = mock(TestCaseResult.class);
        doReturn(new Time().withDuration(123L)).when(first).getTime();

        TestCaseResult second = mock(TestCaseResult.class);
        doReturn(new Time().withDuration(321L)).when(second).getTime();

        aggregator.accumulator().accept(treeData, first);
        aggregator.accumulator().accept(treeData, second);
        assertThat(treeData.getTime(), notNullValue());
        assertThat(treeData.getTime().getMaxDuration(), is(321L));
        assertThat(treeData.getTime().getSumDuration(), is(444L));
        assertThat(treeData.getTime().getStart(), nullValue());
        assertThat(treeData.getTime().getStop(), nullValue());
        assertThat(treeData.getTime().getDuration(), nullValue());
    }

    @Test
    public void shouldBuildTree() throws Exception {
        String uid = UUID.randomUUID().toString();
        TreeData treeData = aggregateResultsWithUids(new OneValueTreeResultAggregator(), uid);

        assertThat(treeData, notNullValue());
        assertThat(treeData.getChildren(), hasSize(1));

        TreeNode treeNode = treeData.getChildren().iterator().next();
        assertThat(treeNode, instanceOf(TestGroupNode.class));

        assertThat(treeNode.getName(), is("sampleGroup"));
        checkNodeHasOnlyOneTestCasesUids(treeNode, uid);
    }

    @Test
    public void shouldGroupByFewValues() throws Exception {
        String uid = UUID.randomUUID().toString();
        TreeData treeData = aggregateResultsWithUids(new FewValuesTreeResultAggregator(), uid);

        assertThat(treeData.getChildren(), hasSize(2));
        treeData.getChildren().forEach(treeNode ->
                checkNodeHasOnlyOneTestCasesUids(treeNode, uid)
        );
    }

    @Test
    public void shouldGroupLikeACharm() throws Exception {
        String uid = UUID.randomUUID().toString();
        TreeData treeData = aggregateResultsWithUids(new ComplexTreeResultAggregator(), uid);
        assertThat(treeData.getChildren(), hasSize(2));
        Set<String> firstLevelGroups = treeData.getChildren().stream()
                .map(TreeNode::getName).collect(Collectors.toSet());
        assertThat(firstLevelGroups, hasItems("a", "b"));

        treeData.getChildren().forEach(treeNode -> {
            assertThat(treeNode, instanceOf(TestGroupNode.class));
            TestGroupNode groupNode = (TestGroupNode) treeNode;
            assertThat(groupNode.getChildren(), hasSize(2));
            Set<String> groups = groupNode.getChildren().stream()
                    .map(TreeNode::getName).collect(Collectors.toSet());
            assertThat(groups, hasItems("1", "2"));
            groupNode.getChildren().forEach(node -> checkNodeHasOnlyOneTestCasesUids(node, uid));
        });
    }

    @Test
    public void shouldCollapseGroupsWithOnlyOneChild() throws Exception {
        String first = UUID.randomUUID().toString();
        String second = UUID.randomUUID().toString();

        TreeData treeData = (TreeData) new TreeCollapseGroupsWithOneChildFinalizer().convert(
                aggregateResultsWithUids(new FewLevelsTreeResultAggregator(), first, second)
        );
        assertThat(treeData.getChildren(), hasSize(1));
        TreeNode firstLevelNode = treeData.getChildren().iterator().next();
        assertThat(firstLevelNode, instanceOf(TestGroupNode.class));
        TestGroupNode groupNode = (TestGroupNode) firstLevelNode;
        assertThat(groupNode.getName(), is("first.second"));
        checkNodeHasOnlyOneTestCasesUids(groupNode, first, second);
    }

    private TreeData aggregateResultsWithUids(TreeResultAggregator aggregator, String... uids) {
        TreeData treeData = aggregator.supplier(null, null).get();
        Stream.of(uids).forEach(uid -> {
            TestCaseResult result = mock(TestCaseResult.class);
            doReturn(uid).when(result).getUid();
            aggregator.accumulator().accept(treeData, result);
        });
        return treeData;
    }

    private void checkNodeHasOnlyOneTestCasesUids(TreeNode treeNode, String... uids) {
        assertThat(treeNode, instanceOf(TestGroupNode.class));
        TestGroupNode groupNode = (TestGroupNode) treeNode;
        assertThat(groupNode.getUid(), not(isEmptyOrNullString()));
        assertThat(groupNode.getTime(), notNullValue());
        assertThat(groupNode.getStatistic(), notNullValue());

        assertThat(groupNode.getChildren(), notNullValue());
        assertThat(groupNode.getChildren(), hasSize(uids.length));
        List<String> actualUids = groupNode.getChildren().stream()
                .filter(TestCaseNode.class::isInstance)
                .map(TestCaseNode.class::cast)
                .map(TestCaseNode::getUid)
                .collect(Collectors.toList());

        assertThat(actualUids, hasItems(uids));
    }

    private class OneValueTreeResultAggregator extends TreeResultAggregator {
        @Override
        protected List<TreeGroup> getGroups(TestCaseResult result) {
            return Collections.singletonList(TreeGroup.values("sampleGroup"));
        }
    }

    private class FewValuesTreeResultAggregator extends TreeResultAggregator {
        @Override
        protected List<TreeGroup> getGroups(TestCaseResult result) {
            return Collections.singletonList(TreeGroup.values("first", "second"));
        }
    }

    private class FewLevelsTreeResultAggregator extends TreeResultAggregator {

        @Override
        protected List<TreeGroup> getGroups(TestCaseResult result) {
            return Arrays.asList(TreeGroup.values("first"), TreeGroup.values("second"));
        }
    }

    private class ComplexTreeResultAggregator extends TreeResultAggregator {
        @Override
        protected List<TreeGroup> getGroups(TestCaseResult result) {
            return Arrays.asList(TreeGroup.values("a", "b"), TreeGroup.values("1", "2"));
        }
    }

}