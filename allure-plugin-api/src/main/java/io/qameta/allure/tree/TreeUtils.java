package io.qameta.allure.tree;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author charlie (Dmitry Baev).
 */
public final class TreeUtils {

    private TreeUtils() {
        throw new IllegalStateException("Do not instance");
    }

    public static String createGroupUid(final String parentUid, final GroupNodeContext groupNodeContext) {
        final MessageDigest md = getMessageDigest();
        md.update(Objects.toString(parentUid).getBytes(UTF_8));
        md.update(Objects.toString(groupNodeContext.getKey()).getBytes(UTF_8));
        md.update(Objects.toString(groupNodeContext.getValue()).getBytes(UTF_8));
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    public static List<Layer> groupByLabels(final TestResult testResult, final LabelName... labelNames) {
        return groupByLabels(testResult, Stream.of(labelNames).map(LabelName::value));
    }

    public static List<Layer> groupByLabels(final TestResult testResult,
                                            final String... labelNames) {
        return groupByLabels(testResult, Stream.of(labelNames));
    }

    public static List<Layer> groupByLabels(final TestResult testResult,
                                            final List<String> labelNames) {
        return groupByLabels(testResult, labelNames.stream());
    }

    public static List<Layer> groupByLabels(final TestResult testResult,
                                            final Stream<String> labelNames) {
        return labelNames
                .map(name -> createLayer(name, testResult))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<Layer> createLayer(final String name, final TestResult testResult) {
        final List<String> values = testResult.findAllLabelValues(name);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Layer(name, values));
    }

    public static Statistic calculateStatisticByLeafs(final TestResultGroupNode group) {
        final Statistic groupStatistic = group.getGroups().stream()
                .reduce(
                        new Statistic(),
                        TreeUtils::updateStatisticRecursive,
                        TreeUtils::mergeStatistic
                );
        final Statistic leafStatistic = group.getLeafs().stream()
                .reduce(
                        new Statistic(),
                        TreeUtils::updateStatisticRecursive,
                        TreeUtils::mergeStatistic
                );
        return mergeStatistic(groupStatistic, leafStatistic);
    }

    public static Statistic calculateStatisticByChildren(final TestResultGroupNode group) {
        return group.getGroups().stream()
                .reduce(
                        new Statistic(),
                        TreeUtils::updateStatistic,
                        TreeUtils::mergeStatistic
                );
    }

    public static Statistic updateStatisticRecursive(final Statistic statistic, final TestResultLeafNode treeNode) {
        statistic.update(treeNode.getStatus());
        return statistic;
    }

    public static Statistic updateStatisticRecursive(final Statistic statistic, final TestResultGroupNode treeNode) {
        statistic.merge(calculateStatisticByLeafs(treeNode));
        return statistic;
    }

    public static Statistic updateStatistic(final Statistic statistic, final TestResultGroupNode treeNode) {
        final Statistic byLeafs = calculateStatisticByLeafs(treeNode);
        statistic.update(byLeafs.getStatus());
        return statistic;
    }

    public static Statistic mergeStatistic(final Statistic a, final Statistic b) {
        final Statistic statistic = new Statistic();
        statistic.merge(a);
        statistic.merge(b);
        return statistic;
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Can not find hashing algorithm", e);
        }
    }
}
