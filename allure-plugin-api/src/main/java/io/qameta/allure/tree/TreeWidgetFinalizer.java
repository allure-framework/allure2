package io.qameta.allure.tree;

import io.qameta.allure.Finalizer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.ExtraStatisticMethods.comparator;

/**
 * @author charlie (Dmitry Baev).
 */
public class TreeWidgetFinalizer implements Finalizer<TreeData> {

    @Override
    public Object convert(final TreeData identity) {
        final List<TreeWidgetItem> items = identity.getChildren().stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .sorted(Comparator.comparing(TestGroupNode::getStatistic, comparator()).reversed())
                .limit(10)
                .map(item -> new TreeWidgetItem()
                        .withUid(item.getUid())
                        .withName(item.getName())
                        .withStatistic(item.getStatistic()))
                .collect(Collectors.toList());
        return new TreeWidgetData()
                .withItems(items)
                .withTotal(identity.getStatistic().getTotal());
    }
}
