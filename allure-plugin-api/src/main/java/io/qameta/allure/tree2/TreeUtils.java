package io.qameta.allure.tree2;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public final class TreeUtils {

    private TreeUtils() {
        throw new IllegalStateException("Do not instance");
    }

    public static List<Classifier<TestResult>> groupByLabels(final TestResult testResult,
                                                             final LabelName... labelNames) {
        return Stream.of(labelNames)
                .map(testResult::findAll)
                .filter(strings -> !strings.isEmpty())
                .map(TestResultClassifier::new)
                .collect(Collectors.toList());
    }

    public static Statistic calculateStatisticByLeafs(final TestResultTreeGroup group) {
        return group.getChildren().stream()
                .reduce(
                        new Statistic(),
                        TreeUtils::updateStatisticRecursive,
                        TreeUtils::mergeStatistic
                );
    }

    public static Statistic calculateStatisticByChildren(final TestResultTreeGroup group) {
        return group.getChildren().stream()
                .reduce(
                        new Statistic(),
                        TreeUtils::updateStatistic,
                        TreeUtils::mergeStatistic
                );
    }

    public static Statistic updateStatisticRecursive(final Statistic statistic, final TreeNode treeNode) {
        if (treeNode instanceof TestResultTreeGroup) {
            statistic.merge(calculateStatisticByLeafs((TestResultTreeGroup) treeNode));
        } else if (treeNode instanceof TestResultTreeLeaf) {
            statistic.update(((TestResultTreeLeaf) treeNode).getStatus());
        }
        return statistic;
    }

    public static Statistic updateStatistic(final Statistic statistic, final TreeNode treeNode) {
        if (treeNode instanceof TestResultTreeGroup) {
            final Statistic byLeafs = calculateStatisticByLeafs((TestResultTreeGroup) treeNode);
            statistic.update(byLeafs.getStatus());
        } else if (treeNode instanceof TestResultTreeLeaf) {
            statistic.update(((TestResultTreeLeaf) treeNode).getStatus());
        }
        return statistic;
    }

    public static Statistic mergeStatistic(final Statistic a, final Statistic b) {
        final Statistic statistic = new Statistic();
        statistic.merge(a);
        statistic.merge(b);
        return statistic;
    }
}
