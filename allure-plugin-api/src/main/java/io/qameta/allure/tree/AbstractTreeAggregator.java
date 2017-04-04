package io.qameta.allure.tree;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestCaseResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author charlie (Dmitry Baev).
 */
public abstract class AbstractTreeAggregator implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        final RandomUidContext randomUidContext = configuration.requireContext(RandomUidContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve(getFileName());
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, getData(randomUidContext.getValue(), launchesResults));
        }
    }

    protected TreeData getData(final Supplier<String> uidGenerator,
                               final List<LaunchResults> launches) {
        final TreeData tree = new TreeData();
        launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .forEach(result -> addResultToTree(uidGenerator, tree, result));
        return postProcess(tree);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void addResultToTree(final Supplier<String> uidGenerator,
                                   final TreeData treeData,
                                   final TestCaseResult result) {
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
                    TestGroupNode groupNode = findGroupByName(groupName, currentLevelGroup.getChildren())
                            .orElseGet(() -> {
                                TestGroupNode newOne = new TestGroupNode()
                                        .withName(groupName)
                                        .withUid(uidGenerator.get());
                                currentLevelGroup.getChildren().add(newOne);
                                return newOne;
                            });
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
    }

    protected Optional<TestGroupNode> findGroupByName(final String groupName, final List<TreeNode> nodes) {
        return nodes.stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .filter(group -> Objects.equals(groupName, group.getName()))
                .findAny();
    }

    protected TreeData postProcess(final TreeData tree) {
        return tree;
    }

    protected String getNodeName(final TestCaseResult result) {
        return result.getName();
    }

    protected abstract String getFileName();

    protected abstract List<TreeGroup> getGroups(final TestCaseResult result);
}
