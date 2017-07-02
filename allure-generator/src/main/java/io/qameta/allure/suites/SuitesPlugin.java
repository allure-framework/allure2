package io.qameta.allure.suites;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Widget;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.TreeWidgetData;
import io.qameta.allure.tree.TreeWidgetItem;
import io.qameta.allure.tree2.DefaultTree;
import io.qameta.allure.tree2.TestResultTreeGroup;
import io.qameta.allure.tree2.TestResultTreeLeaf;
import io.qameta.allure.tree2.Tree;
import io.qameta.allure.tree2.TreeNode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.ExtraStatisticMethods.comparator;
import static io.qameta.allure.entity.LabelName.PARENT_SUITE;
import static io.qameta.allure.entity.LabelName.SUB_SUITE;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.tree2.TreeUtils.groupByLabels;

/**
 * Plugin that generates data for Suites tab.
 *
 * @since 2.0
 */
public class SuitesPlugin implements Aggregator, Widget {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        final RandomUidContext uidContext = configuration.requireContext(RandomUidContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve("suites.json");
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, getData(uidContext.getValue(), launchesResults));
        }
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ Tree<TestResult> getData(final Supplier<String> uidGenerator,
                                           final List<LaunchResults> launchResults) {

        // @formatter:off
        final Tree<TestResult> xunit = new DefaultTree<>(
            "suites",
            testResult -> groupByLabels(uidGenerator, testResult, PARENT_SUITE, SUITE, SUB_SUITE),
            TestResultTreeLeaf::create
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(xunit::add);
        return xunit;
    }

    @Override
    public Object getData(final Configuration configuration, final List<LaunchResults> launches) {
        final Supplier<String> uidGenerator = configuration.requireContext(RandomUidContext.class).getValue();
        final Tree<TestResult> data = getData(uidGenerator, launches);
        final List<TreeWidgetItem> items = data.getChildren().stream()
                .filter(TestResultTreeGroup.class::isInstance)
                .map(TestResultTreeGroup.class::cast)
                .map(this::toWidgetItem)
                .sorted(Comparator.comparing(TreeWidgetItem::getStatistic, comparator()).reversed())
                .limit(10)
                .map(groupNode -> new TreeWidgetItem()
                        .withName(groupNode.getName())
                        .withStatistic(groupNode.getStatistic()))
                .collect(Collectors.toList());
        return new TreeWidgetData().withItems(items).withTotal(data.getChildren().size());
    }

    @Override
    public String getName() {
        return "suites";
    }

    protected TreeWidgetItem toWidgetItem(final TestResultTreeGroup group) {
        return new TreeWidgetItem()
                .withName(group.getName())
                .withUid(group.getUid())
                .withStatistic(calculateStatistic(group));
    }

    protected Statistic calculateStatistic(final TestResultTreeGroup group) {
        return group.getChildren().stream()
                .reduce(
                        new Statistic(),
                        this::update,
                        this::merge
                );
    }

    protected Statistic update(final Statistic statistic, final TreeNode treeNode) {
        if (treeNode instanceof TestResultTreeGroup) {
            statistic.merge(calculateStatistic((TestResultTreeGroup) treeNode));
        } else if (treeNode instanceof TestResultTreeLeaf) {
            statistic.update(((TestResultTreeLeaf) treeNode).getStatus());
        }
        return statistic;
    }

    protected Statistic merge(final Statistic a, final Statistic b) {
        final Statistic statistic = new Statistic();
        statistic.merge(a);
        statistic.merge(b);
        return statistic;
    }

}
