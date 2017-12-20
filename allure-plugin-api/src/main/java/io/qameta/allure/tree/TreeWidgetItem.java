package io.qameta.allure.tree;

import io.qameta.allure.entity.Statistic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

import static io.qameta.allure.tree.TreeUtils.calculateStatisticByLeafs;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class TreeWidgetItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected String name;
    protected Statistic statistic;

    public static TreeWidgetItem create(final TestResultGroupNode groupNode) {
        return new TreeWidgetItem()
                .setUid(groupNode.getUid())
                .setName(groupNode.getName())
                .setStatistic(calculateStatisticByLeafs(groupNode));
    }

}
