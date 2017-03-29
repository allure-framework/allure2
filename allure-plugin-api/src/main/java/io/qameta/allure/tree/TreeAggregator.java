package io.qameta.allure.tree;

import io.qameta.allure.Aggregator;
import io.qameta.allure.JacksonMapperContext;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.ReportConfiguration;
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

import static io.qameta.allure.ReportApiUtils.generateUid;

/**
 * @author charlie (Dmitry Baev).
 */
public abstract class TreeAggregator implements Aggregator {

    @Override
    public void aggregate(ReportConfiguration configuration,
                          List<LaunchResults> launches,
                          Path outputDirectory) throws IOException {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve(getFileName());
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            context.getValue().writeValue(os, getData(launches));
        }
    }

    protected TreeData getData(List<LaunchResults> launches) {
        final TreeData tree = new TreeData();
        launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .forEach(result -> addResultToTree(tree, result));
        return postProcess(tree);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void addResultToTree(TreeData treeData, TestCaseResult result) {
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

    protected TreeData postProcess(TreeData tree) {
        return tree;
    }

    protected String getNodeName(final TestCaseResult result) {
        return result.getName();
    }

    protected abstract String getFileName();

    protected abstract List<TreeGroup> getGroups(final TestCaseResult result);
}
