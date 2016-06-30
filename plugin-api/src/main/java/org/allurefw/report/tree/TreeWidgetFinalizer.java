package org.allurefw.report.tree;

import org.allurefw.report.Finalizer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.allurefw.report.entity.ExtraStatisticMethods.comparator;

/**
 * @author charlie (Dmitry Baev).
 */
public class TreeWidgetFinalizer implements Finalizer<TreeData> {

    @Override
    public Object finalize(TreeData identity) {
        List<TreeWidgetItem> items = identity.getChildren().stream()
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
